package com.runecraftinglclickonly;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RunecraftingLClickOnlyPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RunecraftingLClickOnlyPlugin.class);
		RuneLite.main(args);
	}
}