package fku.org.example.fku.mixin; /* water */

import org.spongepowered.asm.mixin.Mixin;

/**
 * 全枪自动 — 让半自动武器可连续射击
 *
 * ★ 速射现象修复：
 *   不再覆盖 getCoolDown() 返回值（原代码设为 0L 导致武器每 tick 都能射击，产生速射）。
 *   自动射击行为由 TaczSniperFullAutoMixin（重置 lastTimeShootSuccess=false）控制，
 *   武器以自然射速连续射击，不再产生速射。
 *
 * 参考自 Lexis TaczClientShootMixin
 * 该 Mixin 由赛博教员实现
 */
@Mixin(targets = {"com.tacz.guns.client.gameplay.LocalPlayerShoot"}, remap = false)
public class TaczClientShootMixin {

    // ★ 不再覆盖 getCoolDown，武器以自然射速射击
    //    自动射击由 TaczSniperFullAutoMixin（autoShoot → lastTimeShootSuccess=false）驱动
}