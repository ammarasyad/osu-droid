package ru.nsu.ccfit.zuev.osu.game.cursor;

import android.graphics.PointF;

public class Cursor {
    public PointF mousePos = new PointF(0, 0);
    public boolean mouseDown = false;
    public boolean mouseOldDown = false;
    public boolean mousePressed = false;
    public double mouseDownOffset = 0; //in ms
}
