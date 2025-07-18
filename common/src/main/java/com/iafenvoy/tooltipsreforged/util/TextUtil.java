package com.iafenvoy.tooltipsreforged.util;

import com.google.common.collect.Lists;
import com.iafenvoy.tooltipsreforged.mixin.OrderedTextTooltipComponentAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public final class TextUtil {
    public static OrderedText getTextFromComponent(OrderedTextTooltipComponent component) {
        return ((OrderedTextTooltipComponentAccessor) component).getText();
    }

    public static List<MutableText> splitText(MutableText text, int chatWidth, TextRenderer fontRenderer) {
        int i = 0;
        MutableText ichatcomponent = Text.literal("");
        List<MutableText> list = Lists.newArrayList();
        List<MutableText> chatComponents = Lists.newArrayList(text);

        for (int j = 0; j < chatComponents.size(); ++j) {
            MutableText currentChatComponent = chatComponents.get(j);
            String unformattedText = ExtendedTextVisitor.getString(currentChatComponent);
            boolean addToList = false;

            if (unformattedText.contains("\n")) {
                int k = unformattedText.indexOf(10);
                String s1 = unformattedText.substring(k + 1);
                unformattedText = unformattedText.substring(0, k + 1);
                MutableText chatcomponenttext = Text.literal(getColorCode(unformattedText) + s1);
                chatcomponenttext.setStyle(currentChatComponent.getStyle());
                chatComponents.add(j + 1, chatcomponenttext);
                addToList = true;
            }
            String textRemovedLastNewline = unformattedText.endsWith("\n") ? unformattedText.substring(0, unformattedText.length() - 1) : unformattedText;
            int textWidth = fontRenderer.getWidth(textRemovedLastNewline);
            MutableText newChatComponent = Text.literal(textRemovedLastNewline);
            newChatComponent.setStyle(currentChatComponent.getStyle());

            if (i + textWidth > chatWidth) {
                String s2 = fontRenderer.trimToWidth(unformattedText, chatWidth - i, false);
                String s3 = s2.length() < unformattedText.length() ? unformattedText.substring(s2.length()) : null;
                if (s3 != null) {
                    int l = s2.lastIndexOf(" ");
                    if (l >= 0 && fontRenderer.getWidth(unformattedText.substring(0, l)) > 0) {
                        s2 = unformattedText.substring(0, l);
                        s3 = unformattedText.substring(l);
                    } else if (i > 0 && !unformattedText.contains(" ")) {
                        s2 = "";
                        s3 = unformattedText;
                    }
                    MutableText chatcomponenttext2 = Text.literal(getColorCode(s2) + s3);
                    chatcomponenttext2.setStyle(currentChatComponent.getStyle());
                    chatComponents.add(j + 1, chatcomponenttext2);
                }
                textWidth = fontRenderer.getWidth(s2);
                newChatComponent = Text.literal(s2);
                newChatComponent.setStyle(currentChatComponent.getStyle());
                addToList = true;
            }
            if (i + textWidth <= chatWidth) {
                i += textWidth;
                ichatcomponent.append(newChatComponent);
            } else
                addToList = true;
            if (addToList) {
                list.add(ichatcomponent);
                i = 0;
                ichatcomponent = Text.literal("");
            }
        }
        list.add(ichatcomponent);
        return list;
    }

    public static String getColorCode(String s) {
        String color = "";
        StringBuilder format = new StringBuilder();
        char last = 0;
        for (char c : s.toCharArray()) {
            if (last == '§') {
                if (c == 'r' || ('0' <= c && c <= 'f')) {
                    color = "§" + c;
                    format = new StringBuilder();
                } else if ('k' <= c && c <= 'o') {
                    format.append("§").append(c);
                }
            }
            last = c;
        }
        return color + format;
    }

    public static int getColorFromTranslation(Text text) {
        return getColorFromTranslation(text.getString());
    }

    public static int getColorFromTranslation(String text) {
        char[] charArray = text.toCharArray();
        if (charArray.length < 2) return 0xFFFFFFFF;
        for (int i = 0; i < charArray.length - 1; i++) {
            if (charArray[i] == '§') {
                Formatting formatting = Formatting.byCode(charArray[i + 1]);
                int color = formatting == null ? 0xFFFFFFFF : Objects.requireNonNullElse(formatting.getColorValue(), 0xFFFFFFFF);
                if (color != 0xFFFFFFFF) return color;
            }
        }
        return 0xFFFFFFFF;
    }

    public static Text getDurationText(StatusEffectInstance effect, float multiplier) {
        if (effect.isInfinite() || effect.getDuration() >= 60 * 1200) {
            return Text.translatable("effect.duration.infinite");
        } else {
            int i = MathHelper.floor((float) effect.getDuration() * multiplier);
            return Text.literal(StringHelper.formatTicks(i));
        }
    }
}

