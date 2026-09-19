package org.stellarvan.betterelevatorcontact.client;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.elevator.ElevatorContactBlockEntity;
import com.simibubi.create.content.decoration.slidingDoor.DoorControl;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
import org.stellarvan.betterelevatorcontact.Betterelevatorcontact;
import org.stellarvan.betterelevatorcontact.content.WirelessElevatorBehaviour;
import org.stellarvan.betterelevatorcontact.network.ConfigureContactPacket;
import org.stellarvan.betterelevatorcontact.network.ContactNetwork;

import java.util.List;

public final class WirelessContactScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Betterelevatorcontact.MODID,
            "textures/gui/display_link.png");
    private final ElevatorContactBlockEntity contact;
    private final int[] choices = {-2, -2};
    private final ItemStack[] previous = {ItemStack.EMPTY, ItemStack.EMPTY};
    private String shortName;
    private String longName;
    private DoorControl doorMode;
    private EditBox shortInput;
    private EditBox longInput;
    private int left;
    private int top;
    private int selecting = -1;

    public WirelessContactScreen(ElevatorContactBlockEntity contact) {
        super(Component.translatable("create.elevator_contact.title"));
        this.contact = contact;
        shortName = contact.shortName;
        longName = contact.longName;
        doorMode = contact.doorControls.mode;
        var wireless = contact.getBehaviour(WirelessElevatorBehaviour.TYPE);
        if (wireless != null) {
            previous[0] = wireless.frequency(true);
            previous[1] = wireless.frequency(false);
        }
    }

    @Override
    protected void init() {
        left = (width - 263) / 2;
        top = Math.max(8, (height - 190) / 2);
        shortInput = input(left + 23, 28, 4, shortName, true);
        longInput = input(left + 63, 140, 30, longName, false);
        var door = DoorControl.createWidget(left + 58, top + 57, mode -> doorMode = mode, doorMode);
        addRenderableWidget(door.getFirst());
        addRenderableWidget(door.getSecond());
        var confirm = new IconButton(left + 200, top + 58, AllIcons.I_CONFIRM);
        confirm.withCallback(this::confirm);
        addRenderableWidget(confirm);
        setInitialFocus(shortInput);
    }

    private EditBox input(int x, int inputWidth, int max, String value, boolean shortField) {
        var input = new EditBox(font, x, top + 30, inputWidth, 10,
                Component.translatable(shortField ? "create.elevator_contact.floor_identifier"
                        : "create.elevator_contact.floor_description"));
        input.setBordered(false);
        input.setTextColor(0xFFFFFF);
        input.setMaxLength(max);
        input.setValue(value);
        input.setResponder(text -> {
            if (shortField) shortName = text;
            else longName = text;
        });
        addRenderableWidget(input);
        return input;
    }

    private ItemStack frequency(int index) {
        int choice = choices[index];
        if (choice == -2) return previous[index];
        if (choice == -1 || minecraft.player == null) return ItemStack.EMPTY;
        return minecraft.player.getInventory().getItem(choice);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.blit(TEXTURE, left, top, 20, 172, 233, 82);
        graphics.drawCenteredString(font, title, left + 112, top + 6, 0x2F3738);
        GuiGameElement.of(AllBlocks.ELEVATOR_CONTACT.asStack()).<GuiGameElement.GuiRenderBuilder>
                at(left + 239, top + 26, -200).scale(5).render(graphics);
        graphics.renderItem(AllBlocks.TRAIN_DOOR.asStack(), left + 37, top + 58);
        for (int i = 0; i < 2; i++) {
            int x = left + 120 + i * 26;
            drawSlot(graphics, x, top + 58, selecting == i);
            graphics.renderItem(frequency(i), x + 1, top + 59);
        }
        if (selecting >= 0) renderPicker(graphics, mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTick);
        for (int i = 0; i < 2; i++) {
            if (inside(mouseX, mouseY, left + 120 + i * 26, top + 58, 18, 18)) {
                graphics.renderComponentTooltip(font, List.of(
                        Component.translatable("betterelevatorcontact.frequency", i + 1),
                        Component.translatable("betterelevatorcontact.choose"),
                        Component.translatable("betterelevatorcontact.clear")), mouseX, mouseY);
            }
        }
    }

    private void renderPicker(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(left + 20, top + 86, left + 214, top + 190, 0xFFBDBDBD);
        graphics.renderOutline(left + 20, top + 86, 194, 104, 0xFF373737);
        graphics.drawString(font, Component.translatable("betterelevatorcontact.pick", selecting + 1),
                left + 28, top + 91, 0x373737, false);
        if (minecraft.player == null) return;
        ItemStack hovered = ItemStack.EMPTY;
        for (int display = 0; display < 36; display++) {
            int x = left + 35 + display % 9 * 18;
            int y = top + 105 + display / 9 * 18 + (display >= 27 ? 4 : 0);
            int slot = display < 27 ? display + 9 : display - 27;
            ItemStack stack = minecraft.player.getInventory().getItem(slot);
            boolean hover = inside(mouseX, mouseY, x, y, 18, 18);
            drawSlot(graphics, x, y, hover);
            graphics.renderItem(stack, x + 1, y + 1);
            if (hover) hovered = stack;
        }
        if (!hovered.isEmpty()) graphics.renderTooltip(font, hovered, mouseX, mouseY);
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y, boolean selected) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF373737);
        graphics.fill(x + 1, y + 1, x + 18, y + 18, 0xFFF0F0F0);
        graphics.fill(x + 1, y + 1, x + 17, y + 17, selected ? 0xFFA6BFCA : 0xFF8B8B8B);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = 0; i < 2; i++) {
            if (inside(mouseX, mouseY, left + 120 + i * 26, top + 58, 18, 18)) {
                if (button == 1) {
                    choices[i] = -1;
                    selecting = -1;
                } else if (button == 0) selecting = selecting == i ? -1 : i;
                setFocused(null);
                shortInput.setFocused(false);
                longInput.setFocused(false);
                return true;
            }
        }
        if (selecting >= 0 && button == 0 && minecraft.player != null) {
            for (int display = 0; display < 36; display++) {
                int x = left + 35 + display % 9 * 18;
                int y = top + 105 + display / 9 * 18 + (display >= 27 ? 4 : 0);
                if (inside(mouseX, mouseY, x, y, 18, 18)) {
                    int slot = display < 27 ? display + 9 : display - 27;
                    if (!minecraft.player.getInventory().getItem(slot).isEmpty()) {
                        choices[selecting] = slot;
                        selecting = -1;
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_ESCAPE && selecting >= 0) {
            selecting = -1;
            return true;
        }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            confirm();
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    private void confirm() {
        ContactNetwork.CHANNEL.sendToServer(new ConfigureContactPacket(contact.getBlockPos(), shortName, longName,
                doorMode.ordinal(), choices[0], choices[1]));
        onClose();
    }

    @Override
    public void tick() {
        shortInput.tick();
        longInput.tick();
        if (minecraft.player == null || contact.isRemoved() || !contact.canPlayerUse(minecraft.player))
            onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
