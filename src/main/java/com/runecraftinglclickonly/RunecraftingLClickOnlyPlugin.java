package com.runecraftinglclickonly;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import javax.inject.Inject;

@Slf4j
@PluginDescriptor(
    name = "RunecraftingLClickOnly",
    description = "Changes default option for Essence Pouches and Rune Essence in and out of bank for less click intensity",
    tags = {"rune", "essence", "crafting", "runecrafting", "left", "click", "only", "lclick"},
    enabledByDefault = false
)
public class RunecraftingLClickOnlyPlugin extends Plugin {

    @Inject
    private Client client;

    @Override
    protected void startUp() throws Exception {
        log.info("RunecraftingLClickOnly started!");
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("RunecraftingLClickOnly stopped!");
    }

    /** onMenuEntryAdded
     * @param menuEntryAdded
     * Chunk of code taken from Menu Entity Swapper for bank specific actions such as changing the default action of Rune Pouches from deposit to 'Fill'
     * most of the code remains the same.
     */
    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded menuEntryAdded)
    {
        if (menuEntryAdded.getType() == MenuAction.CC_OP.getId()
                && (menuEntryAdded.getIdentifier() == 2 || menuEntryAdded.getIdentifier() == 1)
                && (menuEntryAdded.getOption().startsWith("Deposit-") || menuEntryAdded.getOption().startsWith("Store") || menuEntryAdded.getOption().startsWith("Donate")))
        {

            final int widgetGroupId = WidgetInfo.TO_GROUP(menuEntryAdded.getActionParam1());

            final int opId = //These are the opIds for 0/0/9 (Extra action : Eat, use, etc) and 5/4/8 (Deposit-all) refer to Menu Entity Swapper configs for more context.
                    widgetGroupId == WidgetID.DEPOSIT_BOX_GROUP_ID ? menuEntryAdded.getTarget().contains("pouch") ?
                    0 : 5
                    : widgetGroupId == WidgetID.CHAMBERS_OF_XERIC_STORAGE_UNIT_INVENTORY_GROUP_ID ? menuEntryAdded.getTarget().contains("pouch") ?
                    0 : 4
                    : menuEntryAdded.getTarget().contains("pouch") ?
                    9 : 8;

            final int actionId = opId >= 6 ? MenuAction.CC_OP_LOW_PRIORITY.getId() : MenuAction.CC_OP.getId();

            if (menuEntryAdded.getTarget().contains("rune") || menuEntryAdded.getTarget().contains("pouch")) { //Only use for Runes and Essence Pouches
                bankModeSwap(actionId, opId);
            }
        }

        if (menuEntryAdded.getType() == MenuAction.CC_OP.getId() && menuEntryAdded.getIdentifier() == 1 //Same as above but Withdraw-all for Rune Essence
                && menuEntryAdded.getOption().startsWith("Withdraw"))
        {
            final int widgetGroupId = WidgetInfo.TO_GROUP(menuEntryAdded.getActionParam1());
            final int actionId, opId;
            if (widgetGroupId == WidgetID.CHAMBERS_OF_XERIC_STORAGE_UNIT_PRIVATE_GROUP_ID || widgetGroupId == WidgetID.CHAMBERS_OF_XERIC_STORAGE_UNIT_SHARED_GROUP_ID)
            {
                actionId = MenuAction.CC_OP.getId();
                opId = 4;
            }
            else
            {
                actionId = MenuAction.CC_OP_LOW_PRIORITY.getId();
                opId = 7;
            }

            if (menuEntryAdded.getTarget().contains("essence")) {
                bankModeSwap(actionId, opId);
            }
        }
    }
    //Code also taken directly from Menu Entity Swapper, not edited.
    private void bankModeSwap(int entryTypeId, int entryIdentifier)
    {
        MenuEntry[] menuEntries = client.getMenuEntries();

        for (int i = menuEntries.length - 1; i >= 0; --i)
        {
            MenuEntry entry = menuEntries[i];

            if (entry.getType() == entryTypeId && entry.getIdentifier() == entryIdentifier)
            {
                // Raise the priority of the op so it doesn't get sorted later
                entry.setType(MenuAction.CC_OP.getId());

                menuEntries[i] = menuEntries[menuEntries.length - 1];
                menuEntries[menuEntries.length - 1] = entry;

                client.setMenuEntries(menuEntries);
                break;
            }
        }
    }

    /** onClientTick
     * @param event
     * Sets default action to Empty for rune pouches while outside of bank.
     */
    @Subscribe
    public void onClientTick(ClientTick event) {
        if (client.getGameState() != GameState.LOGGED_IN || client.isMenuOpen()) //If logged in
        {
            return;
        }

        Widget bankContainer = client.getWidget(WidgetInfo.BANK_ITEM_CONTAINER);
        if (bankContainer == null || bankContainer.isSelfHidden()) { //If NOT in Bank

            MenuEntry[] menuEntries = client.getMenuEntries();
            int newIndex = -1;
            int topIndex = menuEntries.length - 1;

            for (int i = topIndex; i >= 0; --i) {
                if (menuEntries[i].getTarget().contains("pouch")) {
                    if (Text.removeTags(menuEntries[i].getOption()).equals("Empty")) { //Find Empty option
                        newIndex = i; //Set to index
                        break;
                    }
                }
            }

            if (newIndex == -1)
            {
                return;
            }

            MenuEntry entry1 = menuEntries[newIndex]; //Swap with default
            MenuEntry entry2 = menuEntries[topIndex];

            menuEntries[newIndex] = entry2;
            menuEntries[topIndex] = entry1;

            client.setMenuEntries(menuEntries); //Set
        }
    }
}
