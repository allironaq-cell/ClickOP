package com.example.clickmacro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Holds the settings and saves/loads them as JSON.
 * File location: <minecraft folder>/config/clickmacro.json
 * Keys are stored as GLFW key codes (numbers), e.g. F = 70, 6 = 54.
 */
public class ClickMacroConfig {
	// ---- Easy-to-change limits ----
	public static final int MIN_CPS = 1;
	public static final int MAX_CPS = 20;

	// ---- Defaults ----
	public static final boolean DEFAULT_RIGHT_CLICK = true;
	public static final int DEFAULT_CPS = 2;
	public static final int DEFAULT_ACTIVATION_KEY = GLFW.GLFW_KEY_F;
	public static final int DEFAULT_MENU_KEY = GLFW.GLFW_KEY_6;

	// ---- Saved values ----
	public boolean rightClick = DEFAULT_RIGHT_CLICK;
	public int cps = DEFAULT_CPS;
	public int activationKey = DEFAULT_ACTIVATION_KEY;
	public int menuKey = DEFAULT_MENU_KEY;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static ClickMacroConfig instance = new ClickMacroConfig();

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("clickmacro.json");
	}

	public static ClickMacroConfig get() {
		return instance;
	}

	/** Reads the file. If anything goes wrong, defaults are used. */
	public static void load() {
		Path path = file();
		if (!Files.exists(path)) {
			instance = new ClickMacroConfig();
			save();
			return;
		}
		try (Reader reader = Files.newBufferedReader(path)) {
			ClickMacroConfig loaded = GSON.fromJson(reader, ClickMacroConfig.class);
			instance = (loaded == null) ? new ClickMacroConfig() : loaded;
		} catch (Exception e) { // broken JSON, unreadable file, etc.
			System.err.println("[ClickMacro] Config broken, using defaults: " + e);
			instance = new ClickMacroConfig();
		}
		instance.sanitize();
		save();
	}

	/** Writes the current settings to the file. */
	public static void save() {
		try (Writer writer = Files.newBufferedWriter(file())) {
			GSON.toJson(instance, writer);
		} catch (IOException e) {
			System.err.println("[ClickMacro] Could not save config: " + e);
		}
	}

	/** Fixes impossible values (e.g. CPS 500, or both keys the same). */
	private void sanitize() {
		cps = Math.max(MIN_CPS, Math.min(MAX_CPS, cps));
		if (activationKey <= 0) activationKey = DEFAULT_ACTIVATION_KEY;
		if (menuKey <= 0) menuKey = DEFAULT_MENU_KEY;
		if (activationKey == menuKey) {
			activationKey = DEFAULT_ACTIVATION_KEY;
			menuKey = DEFAULT_MENU_KEY;
		}
	}
}
