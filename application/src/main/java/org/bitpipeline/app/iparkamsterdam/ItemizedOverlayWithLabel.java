package org.bitpipeline.app.iparkamsterdam;

import java.util.List;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class ItemizedOverlayWithLabel<Item extends OverlayItem> extends ItemizedIconOverlay<Item> {

	// ===========================================================
	// Constants
	// ===========================================================

	public static final int DESCRIPTION_BOX_PADDING = 3;
	public static final int DESCRIPTION_BOX_CORNERWIDTH = 3;

	public static final int DESCRIPTION_LINE_HEIGHT = 20;
	/** Additional to <code>DESCRIPTION_LINE_HEIGHT</code>. */
	public static final int DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT = 2;

	// protected static final Point DEFAULTMARKER_FOCUSED_HOTSPOT = new Point(10, 19);
	protected static final int DEFAULTMARKER_BACKGROUNDCOLOR = Color.rgb(230, 236, 220);

	protected static final int DESCRIPTION_MAXWIDTH = 200;

	// ===========================================================
	// Fields
	// ===========================================================

	protected final int mMarkerFocusedBackgroundColor;
	protected final Paint mMarkerBackgroundPaint, descriptionPaint, titlePaint;

	protected Drawable mMarkerFocusedBase;
	protected int mFocusedItemIndex;
	protected boolean mFocusItemsOnTap;
	private final Point mFocusedScreenCoords = new Point();

	private final String UNKNOWN;

	// ===========================================================
	// Constructors
	// ===========================================================

	public ItemizedOverlayWithLabel(final Context ctx, final List<Item> aList,
			final OnItemGestureListener<Item> aOnItemTapListener) {
		this(aList, aOnItemTapListener, new DefaultResourceProxyImpl(ctx));
	}

	public ItemizedOverlayWithLabel(final List<Item> aList,
			final OnItemGestureListener<Item> aOnItemTapListener, final ResourceProxy pResourceProxy) {
		this(aList, pResourceProxy.getDrawable(ResourceProxy.bitmap.marker_default), null, NOT_SET,
				aOnItemTapListener, pResourceProxy);
	}

	public ItemizedOverlayWithLabel(final List<Item> aList, final Drawable pMarker,
			final Drawable pMarkerFocused, final int pFocusedBackgroundColor,
			final OnItemGestureListener<Item> aOnItemTapListener, final ResourceProxy pResourceProxy) {

		super(aList, pMarker, aOnItemTapListener, pResourceProxy);

		UNKNOWN = mResourceProxy.getString(ResourceProxy.string.unknown);

		if (pMarkerFocused == null) {
			this.mMarkerFocusedBase = boundToHotspot(
					mResourceProxy.getDrawable(ResourceProxy.bitmap.marker_default_focused_base),
					HotspotPlace.BOTTOM_CENTER);
		} else
			this.mMarkerFocusedBase = pMarkerFocused;

		this.mMarkerFocusedBackgroundColor = (pFocusedBackgroundColor != NOT_SET) ? pFocusedBackgroundColor
				: DEFAULTMARKER_BACKGROUNDCOLOR;

		this.mMarkerBackgroundPaint = new Paint(); // Color is set in onDraw(...)

		this.descriptionPaint = new Paint();
		this.descriptionPaint.setAntiAlias(true);
		this.titlePaint = new Paint();
		this.titlePaint.setFakeBoldText(true);
		this.titlePaint.setAntiAlias(true);
		this.unSetFocusedItem();
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public Item getFocusedItem() {
		if (this.mFocusedItemIndex == NOT_SET) {
			return null;
		}
		return this.mItemList.get(this.mFocusedItemIndex);
	}

	public void setFocusedItem(final int pIndex) {
		this.mFocusedItemIndex = pIndex;
	}

	public void unSetFocusedItem() {
		this.mFocusedItemIndex = NOT_SET;
	}

	public void setFocusedItem(final Item pItem) {
		final int indexFound = super.mItemList.indexOf(pItem);
		if (indexFound < 0) {
			throw new IllegalArgumentException();
		}

		this.setFocusedItem(indexFound);
	}

	public void setFocusItemsOnTap(final boolean doit) {
		this.mFocusItemsOnTap = doit;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected boolean onSingleTapUpHelper(final int index, final Item item, final MapView mapView) {
		if (this.mFocusItemsOnTap) {
			this.mFocusedItemIndex = index;
			mapView.postInvalidate();
		}
		return this.mOnItemGestureListener.onItemSingleTapUp(index, item);
	}

	private final Rect mRect = new Rect();

	@Override
	public void draw(final Canvas c, final MapView osmv, final boolean shadow) {
		if (shadow) {
			return;
		}

		for (Item item : super.mItemList) {
			drawItem (c, osmv, item);
		}
	}

	
	public void drawItem (final Canvas c, final MapView osmv, Item item) {
		Drawable markerFocusedBase = item.getMarker(OverlayItem.ITEM_STATE_FOCUSED_MASK);
		if (markerFocusedBase == null) {
			markerFocusedBase = this.mMarkerFocusedBase;
		}

		/* Calculate and set the bounds of the marker. */
		osmv.getProjection().toPixels(item.getPoint (), mFocusedScreenCoords);

		markerFocusedBase.copyBounds(mRect);
		mRect.offset(mFocusedScreenCoords.x, mFocusedScreenCoords.y);

		/* Strings of the OverlayItem, we need. */
		final String itemTitle = (item.getTitle () == null) ? UNKNOWN : item.getTitle ();
		final String itemDescription = (item.getSnippet () == null) ? UNKNOWN
				: item.getSnippet ();

		/*
		 * Store the width needed for each char in the description to a float array. This is pretty
		 * efficient.
		 */
		final float[] widths = new float[itemDescription.length()];
		this.descriptionPaint.getTextWidths(itemDescription, widths);

		final StringBuilder sb = new StringBuilder();
		int maxWidth = 0;
		int curLineWidth = 0;
		int lastStop = 0;
		int i;
		int lastwhitespace = 0;
		/*
		 * Loop through the charwidth array and harshly insert a linebreak, when the width gets
		 * bigger than DESCRIPTION_MAXWIDTH.
		 */
		for (i = 0; i < widths.length; i++) {
			if (!Character.isLetter(itemDescription.charAt(i))) {
				lastwhitespace = i;
			}

			final float charwidth = widths[i];

			if (curLineWidth + charwidth > DESCRIPTION_MAXWIDTH) {
				if (lastStop == lastwhitespace) {
					i--;
				} else {
					i = lastwhitespace;
				}

				sb.append(itemDescription.subSequence(lastStop, i));
				sb.append('\n');

				lastStop = i;
				maxWidth = Math.max(maxWidth, curLineWidth);
				curLineWidth = 0;
			}

			curLineWidth += charwidth;
		}
		/* Add the last line to the rest to the buffer. */
		if (i != lastStop) {
			final String rest = itemDescription.substring(lastStop, i);
			maxWidth = Math.max(maxWidth, (int) this.descriptionPaint.measureText(rest));
			sb.append(rest);
		}
		final String[] lines = sb.toString().split("\n");

		/*
		 * The title also needs to be taken into consideration for the width calculation.
		 */
		final int titleWidth = (int) this.descriptionPaint.measureText(itemTitle);

		maxWidth = Math.max(maxWidth, titleWidth);
		final int descWidth = Math.min(maxWidth, DESCRIPTION_MAXWIDTH);

		/* Calculate the bounds of the Description box that needs to be drawn. */
		final int descBoxLeft = mRect.left - descWidth / 2 - DESCRIPTION_BOX_PADDING
				+ mRect.width() / 2;
		final int descBoxRight = descBoxLeft + descWidth + 2 * DESCRIPTION_BOX_PADDING;
		final int descBoxBottom = mRect.top;
		final int descBoxTop = descBoxBottom - DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT
				- (lines.length + 1) * DESCRIPTION_LINE_HEIGHT /* +1 because of the title. */
				- 2 * DESCRIPTION_BOX_PADDING;

		/* Twice draw a RoundRect, once in black with 1px as a small border. */
		this.mMarkerBackgroundPaint.setColor(Color.BLACK);
		c.drawRoundRect(new RectF(descBoxLeft - 1, descBoxTop - 1, descBoxRight + 1,
				descBoxBottom + 1), DESCRIPTION_BOX_CORNERWIDTH, DESCRIPTION_BOX_CORNERWIDTH,
				this.descriptionPaint);
		this.mMarkerBackgroundPaint.setColor(this.mMarkerFocusedBackgroundColor);
		c.drawRoundRect(new RectF(descBoxLeft, descBoxTop, descBoxRight, descBoxBottom),
				DESCRIPTION_BOX_CORNERWIDTH, DESCRIPTION_BOX_CORNERWIDTH,
				this.mMarkerBackgroundPaint);

		final int descLeft = descBoxLeft + DESCRIPTION_BOX_PADDING;
		int descTextLineBottom = descBoxBottom - DESCRIPTION_BOX_PADDING;

		/* Draw all the lines of the description. */
		for (int j = lines.length - 1; j >= 0; j--) {
			c.drawText(lines[j].trim(), descLeft, descTextLineBottom, this.descriptionPaint);
			descTextLineBottom -= DESCRIPTION_LINE_HEIGHT;
		}
		/* Draw the title. */
		c.drawText(itemTitle, descLeft, descTextLineBottom - DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT,
				this.titlePaint);
		c.drawLine(descBoxLeft, descTextLineBottom, descBoxRight, descTextLineBottom,
				descriptionPaint);

		/*
		 * Finally draw the marker base. This is done in the end to make it look better.
		 */
		Overlay.drawAt(c, markerFocusedBase, mFocusedScreenCoords.x, mFocusedScreenCoords.y, false, 0.0f);
	}

	public void setTitleTextSize (float textSize) {
		this.titlePaint.setTextSize (textSize);
	}

	public void setDescriptionTextSize (float textSize) {
		this.descriptionPaint.setTextSize (textSize);
	}
}
