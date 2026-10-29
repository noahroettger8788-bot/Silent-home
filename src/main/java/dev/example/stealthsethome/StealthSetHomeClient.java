package dev.example.stealthsethome;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public final class StealthSetHomeClient implements ClientModInitializer {
    private static final String COMMAND = "/sethome";
    private static boolean suppressLocalEcho = false;

    @Override
    public void onInitializeClient() {
        KeyBinding key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.stealth_sethome.sethome",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                KeyBinding.Category.MISC
        ));

        // Cancel the local chat HUD echo for the exact command while our keybind sends it.
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (suppressLocalEcho && message.equals("sethome")) {
                suppressLocalEcho = false;
                return false;
            }
            return true;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (key.wasPressed()) {
                if (client.player == null || client.getNetworkHandler() == null) {
                    continue;
                }
                // Send using the normal client command path. The local echo is cancelled above.
                suppressLocalEcho = true;
                client.getNetworkHandler().sendChatCommand("sethome");
                // Safety reset in case this API path does not invoke the allow-chat callback.
                suppressLocalEcho = false;
            }
        });
    }
}
