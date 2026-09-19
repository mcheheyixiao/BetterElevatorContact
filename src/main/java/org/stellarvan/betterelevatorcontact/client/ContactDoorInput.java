package org.stellarvan.betterelevatorcontact.client;

import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import net.minecraft.ChatFormatting;

/** Replaces the inherited scroll hint as well as the options, keeping language overrides consistent. */
final class ContactDoorInput extends SelectionScrollInput {
    private final ContactLanguage language;

    ContactDoorInput(int x, int y, ContactLanguage language) {
        super(x, y, 53, 16);
        this.language = language;
    }

    @Override
    protected void updateTooltip() {
        super.updateTooltip();
        // The superclass may call this before subclass fields have been initialized.
        if (language != null && !toolTip.isEmpty()) {
            toolTip.set(toolTip.size() - 1, language.text("create.gui.scrollInput.scrollToSelect")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }
}
