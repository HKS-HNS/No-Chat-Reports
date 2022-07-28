package com.aizistral.nochatreports.mixins.client;

import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.aizistral.nochatreports.core.NoReportsConfig;

import net.minecraft.client.GuiMessageTag;

@Mixin(GuiMessageTag.class)
public class MixinGuiMessageTag {

	/**
	 * @reason Remove gray bar as indication besides system messages, if mod is configured
	 * respectively. The mod converts all player messages to system messages by default when
	 * installed on server, so we don't need that most of the time.
	 * @author Aizistral
	 */

	@Inject(method = "system", at = @At("HEAD"), cancellable = true)
	private static void onSystem(CallbackInfoReturnable<GuiMessageTag> info) {
		if (NoReportsConfig.hideGrayChatIndicators()) {
			info.setReturnValue(null);
		}
	}

}
