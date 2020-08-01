/*
 * MIT License
 *
 * Copyright (c) NyliumMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.nyliummc.essentials.mod

import com.google.inject.Guice
import com.google.inject.Injector
import io.github.nyliummc.essentials.api.EssentialsMod
import io.github.nyliummc.essentials.api.EssentialsModule
import io.github.nyliummc.essentials.entities.EssentialsDatabase
import io.github.nyliummc.essentials.entities.EssentialsRegistry
import io.github.nyliummc.essentials.entities.LanguageHack
import io.github.nyliummc.essentials.injection.AbstractModule
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager

abstract class AbstractEssentialsMod : EssentialsMod {
    val module = "essentials:modules"
    val logger = LogManager.getLogger(EssentialsMod::class.java)

    override val registry = EssentialsRegistry
    override val database = EssentialsDatabase
    val injector: Injector

    init {
        injector = Guice.createInjector(this.createModule())
    }

    var modules: MutableList<EssentialsModule> = mutableListOf()

    internal fun reload() {
        // TODO:
        // - Maybe reload jars?
        // - Unregister commands
        // - Register commands
        // - Find a solution for reloading configs, which are lazy props rn

        modules.forEach {
            it.onReload()
        }
    }

    fun initialize() {
        FabricLoader.getInstance().allMods.filter { it.metadata.name.contains("essentials") }.forEach { LanguageHack.activate(it.metadata.name) }

        logger.info("Starting Essentials")
        registry.registerBuiltin()
        logger.info("Loading modules")

        val entrypoints = FabricLoader.getInstance().getEntrypointContainers(module, EssentialsModule::class.java)

        // Register events before registering commands
        // in case of a RegisterCommandEvent or something
        entrypoints.forEach {
            it.entrypoint.registerEvents()
        }

        entrypoints.forEach {
            val module = it.entrypoint
            modules.add(module)
            logger.info("Loaded module ${module.name}, provided by ${it.provider.metadata.id}")
            // We need to register configs as early as possible. The actual reloading of configs to handle per world settings can be done after the server has stopped for singleplayer
            // This is due to LiteralTextMixin_Chat accessing the config during a Resource reload.
            // Thereby accessing the essentials instance BEFORE the server start callbacks have been fired
            module.registerConfigs()
            module.registerCommands()
        }

        // TODO: Look into cleanup so we can turn this into internal method references
        ServerLifecycleEvents.SERVER_STARTED.register(ServerLifecycleEvents.ServerStarted { server ->
            database.loadDatabase()

            modules.forEach {
                // Register non-commands
                it.onInitialize()
            }
        })

        ServerLifecycleEvents.SERVER_STOPPED.register(ServerLifecycleEvents.ServerStopped { server ->
            // Disable DB, unregister everything except commands
            database.disconnect()
        })
    }

    abstract fun createModule(): AbstractModule
}
