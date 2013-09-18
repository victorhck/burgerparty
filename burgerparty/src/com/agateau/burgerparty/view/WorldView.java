package com.agateau.burgerparty.view;

import java.util.HashSet;

import com.agateau.burgerparty.BurgerPartyGame;
import com.agateau.burgerparty.model.Customer;
import com.agateau.burgerparty.model.MealItem;
import com.agateau.burgerparty.model.LevelResult;
import com.agateau.burgerparty.model.World;

import com.agateau.burgerparty.screens.GameScreen;
import com.agateau.burgerparty.utils.Anchor;
import com.agateau.burgerparty.utils.Signal0;
import com.agateau.burgerparty.utils.Signal1;
import com.agateau.burgerparty.utils.UiUtils;
import com.agateau.burgerparty.view.InventoryView;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;

public class WorldView extends AbstractWorldView {
	public WorldView(GameScreen screen, BurgerPartyGame game, World world, TextureAtlas atlas, Skin skin) {
		super(world.getLevelWorld());
		setFillParent(true);
		setSpacing(UiUtils.SPACING);
		mGameScreen = screen;
		mGame = game;
		mWorld = world;
		mAtlas = atlas;
		mSkin = skin;
		mCustomerFactory = new CustomerViewFactory(atlas, Gdx.files.internal("customerparts.xml"));

		setupCustomers();
		setupTargetMealView();
		setupInventoryView();
		setupHud();
		setupAnchors();

		mWorld.burgerFinished.connect(mHandlers, new Signal0.Handler() {
			public void handle() {
				onBurgerFinished();
			}
		});
		mWorld.mealFinished.connect(mHandlers, new Signal1.Handler<World.Score>() {
			public void handle(World.Score score) {
				onMealFinished(score);
			}
		});
		mWorld.levelFailed.connect(mHandlers, new Signal0.Handler() {
			public void handle() {
				showGameOverOverlay();
			}
		});
		mWorld.trashing.connect(mHandlers, new Signal0.Handler() {
			@Override
			public void handle() {
				onTrashing();
			}
		});

		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				goToNextCustomer();
			}
		});
	}

	public void onTrashing() {
		mInventoryView.setInventory(mWorld.getBurgerInventory());
		Timer.schedule(
			new Timer.Task() {
				@Override
				public void run() {
					mWorld.markTrashingDone();
				}
			}, MealView.TRASH_ACTION_DURATION);
	}

	public void onBackPressed() {
		pause();
	}

	public void pause() {
		if (mGameScreen.getOverlay() != null) {
			// This can happen when called from GameScreen.pause()
			return;
		}
		mWorld.pause();
		mGameScreen.setOverlay(new PauseOverlay(this, mGame, mAtlas, mSkin));
	}

	public void resume() {
		mGameScreen.setOverlay(null);
		mWorld.resume();
	}

	public InventoryView getInventoryView() {
		return mInventoryView;
	}

	protected void onResized() {
		if (mActiveCustomerView != null) {
			updateBubbleGeometry();
		}
		updateCustomerPositions();
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		updateTimerDisplay();
	}

	private void setupCustomers() {
		for (Customer customer: mWorld.getCustomers()) {
			CustomerView customerView = mCustomerFactory.create(customer);
			customerView.setX(-customerView.getWidth());
			mWaitingCustomerViews.add(customerView);
		}
		// Add actors starting from the end of the list so that the Z order is correct
		// (mWaitingCustomerViews[0] is in front of mWaitingCustomerViews[1])
		for (int i = mWaitingCustomerViews.size - 1; i >= 0; --i) {
			addActor(mWaitingCustomerViews.get(i));
		}
	}

	private void setupTargetMealView() {
		mBubble = new Bubble(mAtlas.createPatch("ui/bubble-callout-left"));
		addActor(mBubble);
		mTargetMealView = new MealView(mWorld.getTargetBurger(), mWorld.getTargetMealExtra(), mAtlas, false);
		mTargetMealView.getBurgerView().setPadding(8);
		mTargetMealView.setScale(0.5f, 0.5f);
		mBubble.setChild(mTargetMealView);
		mBubble.setVisible(false);
	}

	private void setupInventoryView() {
		mInventoryView.setInventory(mWorld.getBurgerInventory());
		mInventoryView.itemSelected.connect(mHandlers, new Signal1.Handler<MealItem>() {
			@Override
			public void handle(MealItem item) {
				if (!mWorld.isTrashing()) {
					mMealView.addItem(item);
				}
			}
		});
	}

	private void setupMealView() {
		mMealView = new MealView(mWorld.getBurger(), mWorld.getMealExtra(), mAtlas, true);
		createMealViewAnchorRule(mMealView);
	}

	private void setupHud() {
		mHudImage = new Image(mAtlas.findRegion("ui/hud-bg"));
		mScoreDisplay = new Label("0", mSkin, "score");
		updateScoreDisplay();
		mTimerDisplay = new Label("0", mSkin, "timer");
		mPauseButton = new Image(mAtlas.findRegion("ui/pause"));

		ClickListener listener = new ClickListener() {
			public void clicked(InputEvent event, float x, float y) {
				pause();
			}
		};
		for (Actor actor: new Actor[]{mHudImage, mTimerDisplay, mScoreDisplay, mPauseButton}) {
			actor.setTouchable(Touchable.enabled);
			actor.addListener(listener);
		}
	}

	private void setupAnchors() {
		addRule(mHudImage, Anchor.TOP_LEFT, this, Anchor.TOP_LEFT, 0, 0);
		addRule(mPauseButton, Anchor.TOP_LEFT, this, Anchor.TOP_LEFT, 0.7f, -0.6f);
		addRule(mTimerDisplay, Anchor.CENTER_LEFT, mPauseButton, Anchor.CENTER_LEFT, 1.2f, 0);
		addRule(mScoreDisplay, Anchor.TOP_LEFT, this, Anchor.TOP_LEFT, 0.7f, -1.6f);
	}

	private void updateScoreDisplay() {
		String txt = String.format("%07d", mWorld.getScore());
		mScoreDisplay.setText(txt);
		UiUtils.adjustToPrefSize(mScoreDisplay);
	}

	private void updateTimerDisplay() {
		int total = mWorld.getRemainingSeconds();
		int minutes = total / 60;
		int seconds = total % 60;
		String txt = String.format("%d:%02d", minutes, seconds);
		if (txt.contentEquals(mTimerDisplay.getText())) {
			return;
		}
		if (total >= 20) {
			mTimerDisplay.setColor(Color.WHITE);
		} else {
			mTimerDisplay.setColor(Color.RED);
			mTimerDisplay.addAction(Actions.color(Color.WHITE, 0.5f));
		}
		mTimerDisplay.setText(txt);
		UiUtils.adjustToPrefSize(mTimerDisplay);
	}

	private void showGameOverOverlay() {
		mGameScreen.setOverlay(new GameOverOverlay(mGame, mAtlas, mSkin));
	}

	private void slideDoneMealView(Runnable toDoAfter) {
		mDoneMealView = mMealView;
		removeRulesForActor(mDoneMealView);
		mDoneMealView.addAction(
			Actions.sequence(
				Actions.moveTo(getWidth(), mDoneMealView.getY(), 0.4f, Interpolation.pow2In),
				Actions.removeActor()
			)
		);
		mBubble.setVisible(false);
		mActiveCustomerView.addAction(
			Actions.sequence(
				Actions.moveTo(getWidth(), mActiveCustomerView.getY(), 0.4f, Interpolation.pow2In),
				Actions.run(toDoAfter),
				Actions.removeActor()
			)
		);
		mActiveCustomerView = null;
	}

	private void onBurgerFinished() {
		mInventoryView.setInventory(mWorld.getMealExtraInventory());
	}

	private void onMealFinished(World.Score score) {
		mActiveCustomerView.getCustomer().setState(Customer.State.SERVED);
		updateScoreDisplay();
		float x = mMealView.getX() + mMealView.getBurgerView().getWidth() / 2;
		float y = mMealView.getY() + mMealView.getBurgerView().getHeight();
		new ScoreFeedbackActor(this, x, y, score);
		slideDoneMealView(new Runnable() {
			@Override
			public void run() {
				if (mWaitingCustomerViews.size > 0) {
					goToNextCustomer();
				} else {
					showLevelFinishedOverlay();
				}
			}
		});
	}

	private void showLevelFinishedOverlay() {
		LevelResult result = mWorld.getLevelResult();
		mGameScreen.setOverlay(new LevelFinishedOverlay(mGame, result, mAtlas, mSkin));
	}

	private void goToNextCustomer() {
		setupMealView();
		mInventoryView.setInventory(mWorld.getBurgerInventory());
		mActiveCustomerView = mWaitingCustomerViews.removeIndex(0);
		mActiveCustomerView.getCustomer().setState(Customer.State.ACTIVE);
		updateCustomerPositions();
	}

	private void updateCustomerPositions() {
		Array<CustomerView> customerViews = new Array<CustomerView>(mWaitingCustomerViews);
		if (mActiveCustomerView != null) {
			customerViews.insert(0, mActiveCustomerView);
		}
		float centerX = getWidth() / 2;
		float posY = MathUtils.ceil(mWorkbench.getTop() - 4);
		final float padding = 10;
		float delay = 0;
		for(CustomerView customerView: customerViews) {
			float width = customerView.getWidth();
			customerView.addAction(
				Actions.sequence(
					Actions.moveTo(customerView.getX(), posY), // Force posY to avoid getting from under the workbench at startup
					Actions.delay(delay),
					Actions.moveTo(MathUtils.ceil(centerX - width / 2), posY, 0.3f, Interpolation.sineOut)
				)
			);
			centerX -= width + padding;
			delay += 0.1;
		}
		if (mActiveCustomerView != null) {
			Action doShowBubble = Actions.run(new Runnable() {
				@Override
				public void run() {
					showBubble();
				}
			});
			mActiveCustomerView.addAction(Actions.after(doShowBubble));
		}
	}

	private void showBubble() {
		mBubble.setVisible(true);
		updateBubbleGeometry();
	}

	private void updateBubbleGeometry() {
		mBubble.setPosition(MathUtils.ceil(mActiveCustomerView.getRight() - 10), MathUtils.ceil(mActiveCustomerView.getY() + 50));
		mBubble.updateGeometry();
	}

	private HashSet<Object> mHandlers = new HashSet<Object>();

	private GameScreen mGameScreen;
	private BurgerPartyGame mGame;
	private World mWorld;
	private TextureAtlas mAtlas;
	private Skin mSkin;
	private MealView mMealView;
	private MealView mDoneMealView;
	private MealView mTargetMealView;
	private Label mTimerDisplay;
	private Label mScoreDisplay;
	private Image mHudImage;
	private Image mPauseButton;
	private Bubble mBubble;
	private CustomerViewFactory mCustomerFactory;
	private Array<CustomerView> mWaitingCustomerViews = new Array<CustomerView>();
	private CustomerView mActiveCustomerView;
}