package hz.cdj.game.fmj.combat.actions;

import android.graphics.Canvas;
import hz.cdj.game.fmj.characters.FightingCharacter;
import hz.cdj.game.fmj.characters.Player;
import hz.cdj.game.fmj.combat.anim.RaiseAnimation;
import hz.cdj.game.fmj.goods.BaseGoods;
import hz.cdj.game.fmj.goods.GoodsHiddenWeapon;
import hz.cdj.game.fmj.goods.GoodsWeapon;
import hz.cdj.game.fmj.lib.ResSrs;

public class ActionThrowItemOne extends ActionSingleTarget {

	private static final int STATE_PRE = 1; // 起手动画
	private static final int STATE_ANI = 2; // 魔法动画
	private static final int STATE_AFT = 3; // 伤害动画

	private int mState = 1;

	private ResSrs mAni;

	private int mAniX, mAniY;

	private int ox, oy;

	BaseGoods weapon;

	/** 伤害生命值 */
	int affectHp;

	public ActionThrowItemOne(FightingCharacter attacker, FightingCharacter target, BaseGoods g) {
		super(attacker, target);
		this.weapon = g;
		if (g instanceof GoodsHiddenWeapon) {
			GoodsHiddenWeapon hiddenWeapon = (GoodsHiddenWeapon) g;
			affectHp = hiddenWeapon.getAffectHp();
			mAni = hiddenWeapon.getAni();
			if (mAni != null) {
				mAni.startAni();
				mAni.setIteratorNum(2);
			}
		} else {
			GoodsWeapon weapon = (GoodsWeapon) g;
			affectHp = 1034;
		}
	}

	@Override
	public void preproccess() {
		// TODO 记下伤害值、异常状态 there is null pointer
		ox = mAttacker.getCombatX();
		oy = mAttacker.getCombatY();

		// TODO effect it
		mTarget.setHP(mTarget.getHP() - affectHp);
		// 使用掉
		Player.sGoodsList.deleteGoods(weapon);

		mAniX = mTarget.getCombatX();
		mAniY = mTarget.getCombatY();
		mRaiseAni = new RaiseAnimation(mAniX, mTarget.getCombatTop(), -affectHp, 0);
	}

	@Override
	public boolean update(long delta) {
		super.update(delta);
		switch (mState) {
		case STATE_PRE:
			if (mCurrentFrame < 10) {
				if (mAttacker instanceof Player) {
					mAttacker.getFightingSprite().setCurrentFrame(
						mCurrentFrame * 3 / 10 + 6);
				} else {
					mAttacker.setCombatPos(ox + 2, oy + 2);
				}
			} else {
				mState = STATE_ANI;
			}
			break;

		case STATE_ANI:
			if (mAni == null || !mAni.update(delta)) {
				mState = STATE_AFT;
				if (mAttacker instanceof Player) {
					((Player)mAttacker).setFrameByState();
				} else {
					mAttacker.getFightingSprite().move(-2, -2);
				}
				if (mTarget instanceof Player) {
					mTarget.getFightingSprite().setCurrentFrame(10);
				} else {
					mTarget.getFightingSprite().move(2, 2);
				}
			}
			break;

		case STATE_AFT:
			if (!mRaiseAni.update(delta)) {
				if (mTarget instanceof Player) {
					((Player)mTarget).setFrameByState();
				} else {
					mTarget.getFightingSprite().move(-2, -2);
				}
				return false;
			}
			break;
		}
		return true;
	}

	@Override
	public void draw(Canvas canvas) {
		if (mState == STATE_ANI && mAni != null) {
			mAni.drawAbsolutely(canvas, mAniX, mAniY);
		} else if (mState == STATE_AFT) {
			mRaiseAni.draw(canvas);
		}
	}

}
