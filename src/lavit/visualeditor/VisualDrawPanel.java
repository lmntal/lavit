/*
 *   Copyright (c) 2008, Ueda Laboratory LMNtal Group <lmntal@ueda.info.waseda.ac.jp>
 *   All rights reserved.
 *
 *   Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are
 *   met:
 *
 *    1. Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *
 *    3. Neither the name of the Ueda Laboratory LMNtal Group nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *   OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package lavit.visualeditor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import lavit.Env;
import lavit.FrontEnd;
import lavit.util.CommonFontUser;

@SuppressWarnings("serial")
public class VisualDrawPanel extends JPanel
		implements MouseInputListener, MouseWheelListener, KeyListener, CommonFontUser {
	VisualPanel visualPanel;

	private int gridInterval;
	private double zoom;
	private double displayX, displayY;

	private Point startPoint;
	private Point lastPoint;
	private Point nowPoint;
	private double remainMoveX, remainMoveY;

	private VisualDrawPainter painter;

	ArrayList<VisualSrcMem> mems;
	ArrayList<VisualSrcAtom> atoms;
	ArrayList<VisualSrcLink> links;

	private boolean selectActioned;
	ArrayList<VisualSrcMem> selectMems;
	ArrayList<VisualSrcAtom> selectAtoms;
	ArrayList<VisualSrcLink> selectLinks;

	VisualDrawPanel(VisualPanel visualPanel) {
		this.visualPanel = visualPanel;

		setFocusable(true);
		setLayout(new BorderLayout());

		painter = new VisualDrawPainter(this);
		painter.start();

		loadFont();
		FrontEnd.addFontUser(this);

	}

	public void init() {

		this.gridInterval = 50;
		this.zoom = 1.0;
		this.displayX = 0;
		this.displayY = 0;

		this.startPoint = null;
		this.lastPoint = null;
		this.nowPoint = null;
		this.remainMoveX = 0;
		this.remainMoveY = 0;

		this.mems = new ArrayList<VisualSrcMem>();
		this.atoms = new ArrayList<VisualSrcAtom>();
		this.links = new ArrayList<VisualSrcLink>();

		this.selectActioned = false;
		this.selectMems = new ArrayList<VisualSrcMem>();
		this.selectAtoms = new ArrayList<VisualSrcAtom>();
		this.selectLinks = new ArrayList<VisualSrcLink>();

		setActive(true);

	}

	void update() {
		painter.update();
	}

	void setActive(boolean active) {
		if (painter.isActive() == active) {
			return;
		}
		if (active) {
			addMouseListener(this);
			addMouseMotionListener(this);
			addMouseWheelListener(this);
			addKeyListener(this);
		} else {
			removeMouseListener(this);
			removeMouseMotionListener(this);
			removeMouseWheelListener(this);
			removeKeyListener(this);
		}
		painter.setActive(active);
		visualPanel.toolBar.allButtonSetEnabled(active);
		visualPanel.controlPanel.allButtonSetEnabled(active);
		if (active) {
			update();
		} else {
			repaint();
		}
	}

	public void loadFont() {
		Font font = new Font(Env.get("EDITER_FONT_FAMILY"), Font.PLAIN, Env.getInt("EDITER_FONT_SIZE"));

		revalidate();
	}

	void setZoom(double zoom) {
		if (zoom < 0.01) {
			zoom = 0.01;
		} else if (zoom > 4.0) {
			zoom = 4.0;
		}

		double w = (double) getWidth() / 2;
		double h = (double) getHeight() / 2;
		double newW = w / zoom;
		double newH = h / zoom;
		double oldW = w / this.zoom;
		double oldH = h / this.zoom;

		displayX += newW - oldW;
		displayY += newH - oldH;

		this.zoom = zoom;
	}

	public void setSliderZoom(double zoom) {
		setZoom(zoom);
		visualPanel.controlPanel.setSliderPos(zoom);
	}

	public double getZoom() {
		return this.zoom;
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		// フレームの初期化
		g2.setColor(Color.white);
		g2.fillRect(0, 0, getWidth(), getHeight());

		if (!painter.isActive()) {
			return;
		}

		g2.scale(zoom, zoom);

		drawGrid(g2);

		drawMems(g2);
		drawLinks(g2);
		drawAtoms(g2);

		if (visualPanel.toolBar.selected == visualPanel.toolBar.atomButton) {
			if (nowPoint != null) {
				Point p = real2grid(display2real(nowPoint));
				drawSrcAtom(g2, p, isOver(p) ? Color.red : Color.blue);
			}
		} else if (visualPanel.toolBar.selected == visualPanel.toolBar.memButton) {
			if (startPoint != null) {
				Rectangle r = getDragRectangle();
				if (r.width * r.height > 0) {
					drawSrcMem(g2, r.x, r.y, r.width, r.height, isOver(r.x, r.y, r.width, r.height) ? Color.red : Color.blue);
				}
			}
		} else if (visualPanel.toolBar.selected == visualPanel.toolBar.linkButton) {
			if (startPoint != null) {
				Point p1 = real2grid(display2real(startPoint));
				Point p2 = real2grid(display2real(lastPoint));
				VisualSrcAtom atom1 = null;
				VisualSrcAtom atom2 = null;
				for (VisualSrcAtom atom : atoms) {
					if (p1.equals(atom.getPoint())) {
						atom1 = atom;
						break;
					}
				}
				for (VisualSrcAtom atom : atoms) {
					if (p2.equals(atom.getPoint())) {
						atom2 = atom;
						break;
					}
				}
				if (atom1 != null) {
					drawSrcAtom(g2, atom1, Color.yellow);
					if (atom2 != null) {
						drawSrcAtom(g2, atom2.getPoint(), Color.yellow);
					}
					drawSrcLink(g2, p1, p2, (!(atom1 != null && atom2 != null)) ? Color.red : Color.blue);
				}
			} else if (nowPoint != null) {
				Point p = real2grid(display2real(nowPoint));
				for (VisualSrcAtom atom : atoms) {
					if (p.equals(atom.getPoint())) {
						drawSrcAtom(g2, p, Color.yellow);
						break;
					}
				}
			}
		}

		g2.setColor(Color.black);
		g2.drawOval((int) displayX - 1, (int) displayY - 1, 2, 2);

		g2.scale(1.0 / zoom, 1.0 / zoom);

	}

	private void drawMems(Graphics2D g2) {
		for (VisualSrcMem mem : mems) {
			drawSrcMem(g2, mem, selectMems.contains(mem) ? Color.red : Color.blue);
		}
	}

	private void drawLinks(Graphics2D g2) {
		for (VisualSrcLink link : links) {
			drawSrcLink(g2, link.atom1, link.atom2, selectLinks.contains(link) ? Color.red : Color.black);
		}
	}

	private void drawAtoms(Graphics2D g2) {
		for (VisualSrcAtom atom : atoms) {
			drawSrcAtom(g2, atom, selectAtoms.contains(atom) ? Color.red : Color.blue);
			g2.drawString(atom.str, atom.x * gridInterval + 3 + (int) displayX, atom.y * gridInterval - 18 + (int) displayY);
			if (atom.links.size() >= 2) {
				int no = 0;
				for (VisualSrcLink link : atom.links) {
					int toX, toY;
					if (link.atom1 != atom) {
						toX = link.atom1.x;
						toY = link.atom1.y;
					} else {
						toX = link.atom2.x;
						toY = link.atom2.y;
					}
					double theta = Math.atan2(toY - atom.y, toX - atom.x);
					double cos = Math.cos(theta);
					double sin = Math.sin(theta);
					g2.drawString("" + (no++), atom.x * gridInterval + (int) displayX + (int) (30 * cos),
							atom.y * gridInterval + (int) displayY + (int) (30 * sin));
				}
			}

		}
	}

	RoundRectangle2D.Double getAtomShape(int x, int y) {
		double r = 15;
		return new RoundRectangle2D.Double(grid2drawX(x) - r, grid2drawY(y) - r, r * 2, r * 2, r * 2, r * 2);
	}

	RoundRectangle2D.Double getAtomShape(VisualSrcAtom atom) {
		return getAtomShape(atom.x, atom.y);
	}

	RoundRectangle2D.Double getMemShape(int x, int y, int w, int h) {
		return new RoundRectangle2D.Double(grid2drawX(x), grid2drawY(y), w * gridInterval, h * gridInterval, 25, 25);
	}

	RoundRectangle2D.Double getMemShape(VisualSrcMem mem) {
		return getMemShape(mem.x, mem.y, mem.width, mem.height);
	}

	Line2D.Double getLinkShape(int x1, int y1, int x2, int y2) {
		return new Line2D.Double(grid2drawX(x1), grid2drawY(y1), grid2drawX(x2), grid2drawY(y2));
	}

	Line2D.Double getLinkShape(VisualSrcLink link) {
		return getLinkShape(link.atom1.x, link.atom1.y, link.atom2.x, link.atom2.y);
	}

	private void drawSrcAtom(Graphics2D g2, int x, int y, Color c) {
		Shape s = getAtomShape(x, y);
		g2.setColor(c);
		g2.fill(s);
		g2.setColor(Color.gray);
		g2.setStroke(new BasicStroke(3.0f));
		g2.draw(s);
	}

	private void drawSrcAtom(Graphics2D g2, VisualSrcAtom atom, Color c) {
		drawSrcAtom(g2, atom.x, atom.y, c);
	}

	private void drawSrcAtom(Graphics2D g2, Point gp, Color c) {
		drawSrcAtom(g2, gp.x, gp.y, c);
	}

	private void drawSrcMem(Graphics2D g2, int x, int y, int w, int h, Color c) {
		Shape s = getMemShape(x, y, w, h);
		g2.setColor(c);
		g2.setStroke(new BasicStroke(3.0f));
		g2.draw(s);
	}

	private void drawSrcMem(Graphics2D g2, VisualSrcMem mem, Color c) {
		drawSrcMem(g2, mem.x, mem.y, mem.width, mem.height, c);
	}

	private void drawSrcLink(Graphics2D g2, int x1, int y1, int x2, int y2, Color c) {
		Shape s = getLinkShape(x1, y1, x2, y2);
		g2.setColor(c);
		g2.setStroke(new BasicStroke(2.0f));
		g2.draw(s);
	}

	private void drawSrcLink(Graphics2D g2, VisualSrcLink link, Color c) {
		drawSrcLink(g2, link.atom1.x, link.atom1.y, link.atom2.x, link.atom2.y, c);
	}

	private void drawSrcLink(Graphics2D g2, VisualSrcAtom atom1, VisualSrcAtom atom2, Color c) {
		drawSrcLink(g2, atom1.x, atom1.y, atom2.x, atom2.y, c);
	}

	private void drawSrcLink(Graphics2D g2, Point gp1, Point gp2, Color c) {
		drawSrcLink(g2, gp1.x, gp1.y, gp2.x, gp2.y, c);
	}

	Point2D.Double display2real(Point dp) {
		return new Point2D.Double(display2realX(dp.x), display2realY(dp.y));
	}

	double display2realX(int x) {
		return x / zoom - displayX;
	}

	double display2realY(int y) {
		return y / zoom - displayY;
	}

	Point2D.Double grid2draw(Point gp) {
		return new Point2D.Double(grid2drawX(gp.x), grid2drawY(gp.y));
	}

	double grid2drawX(int x) {
		return x * gridInterval + displayX;
	}

	double grid2drawY(int y) {
		return y * gridInterval + displayY;
	}

	Point real2grid(Point2D.Double rp) {
		return new Point(real2gridX(rp.x), real2gridY(rp.y));
	}

	int real2gridX(double x) {
		return (int) ((x + (x > 0 ? 1 : -1) * (gridInterval / 2)) / gridInterval);
	}

	int real2gridY(double y) {
		return (int) ((y + (y > 0 ? 1 : -1) * (gridInterval / 2)) / gridInterval);
	}

	Point2D.Double real2draw(Point2D.Double rp) {
		return new Point2D.Double(real2drawX(rp.x), real2drawY(rp.y));
	}

	double real2drawX(double x) {
		return x + displayX;
	}

	double real2drawY(double y) {
		return y + displayY;
	}

	private boolean isOver(Point p) {
		return isOver(p.x, p.y);
	}

	private boolean isOver(int x, int y) {
		for (VisualSrcMem mem : mems) {
			for (int mx = mem.x; mx <= mem.x + mem.width; ++mx) {
				if (x == mx && (y == mem.y || y == mem.y + mem.height)) {
					return true;
				}
			}
			for (int my = mem.y; my <= mem.y + mem.height; ++my) {
				if (y == my && (x == mem.x || x == mem.x + mem.width)) {
					return true;
				}
			}
		}
		for (VisualSrcAtom atom : atoms) {
			if (atom.x == x && atom.y == y) {
				return true;
			}
		}
		return false;
	}

	private boolean isOver(int x, int y, int w, int h) {
		class Line {
			Point p1, p2;

			Line(Point p1, Point p2) {
				this.p1 = p1;
				this.p2 = p2;
			}

			boolean isCross(Line l) {
				int c1 = (p1.x - p2.x) * (l.p1.y - p1.y) + (p1.y - p2.y) * (p1.x - l.p1.x);
				int c2 = (p1.x - p2.x) * (l.p2.y - p1.y) + (p1.y - p2.y) * (p1.x - l.p2.x);
				int c3 = (l.p1.x - l.p2.x) * (p1.y - l.p1.y) + (l.p1.y - l.p2.y) * (l.p1.x - p1.x);
				int c4 = (l.p1.x - l.p2.x) * (p2.y - l.p1.y) + (l.p1.y - l.p2.y) * (l.p1.x - p2.x);
				if (c1 * c2 <= 0 && c3 * c4 <= 0 && !(c1 == 0 && c2 == 0 && c3 == 0 && c4 == 0)) {
					return true;
				}
				return false;
			}
		}

		Line[] lines = { new Line(new Point(x, y), new Point(x + w, y)), new Line(new Point(x, y), new Point(x, y + h)),
				new Line(new Point(x, y + h), new Point(x + w, y + h)),
				new Line(new Point(x + w, y), new Point(x + w, y + h)) };

		for (VisualSrcMem mem : mems) {
			Line[] mlines = { new Line(new Point(mem.x, mem.y), new Point(mem.x + mem.width, mem.y)),
					new Line(new Point(mem.x, mem.y), new Point(mem.x, mem.y + mem.height)),
					new Line(new Point(mem.x, mem.y + mem.height), new Point(mem.x + mem.width, mem.y + mem.height)),
					new Line(new Point(mem.x + mem.width, mem.y), new Point(mem.x + mem.width, mem.y + mem.height)) };
			for (Line l : lines) {
				for (Line ml : mlines) {
					if (l.isCross(ml)) {
						return true;
					}
				}
			}
		}
		for (VisualSrcAtom atom : atoms) {
			for (int mx = x; mx <= x + w; ++mx) {
				if (atom.x == mx && (atom.y == y || atom.y == y + h)) {
					return true;
				}
			}
			for (int my = y; my <= y + h; ++my) {
				if (atom.y == my && (atom.x == x || atom.x == x + w)) {
					return true;
				}
			}
		}
		return false;
	}

	private void drawGrid(Graphics2D g2) {
		double minX = 0, maxX = getWidth() / zoom;
		double minY = 0, maxY = getHeight() / zoom;

		g2.setColor(new Color(230, 230, 230));
		for (double x = minX + displayX % gridInterval - gridInterval; x <= maxX; x += gridInterval) {
			drawLine(g2, x, minY, x, maxY);
		}
		for (double y = minY + displayY % gridInterval - gridInterval; y <= maxY; y += gridInterval) {
			drawLine(g2, minX, y, maxX, y);
		}
	}

	private void drawLine(Graphics2D g2, double x1, double y1, double x2, double y2) {
		if (zoom > 2.0) {
			// doubleライン
			g2.draw(new Line2D.Double(x1, y1, x2, y2));
		} else {
			// intライン
			g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
		}
	}

	private void drawArc(Graphics2D g2, double x, double y, double w, double h, double start, double extent) {
		if (zoom > 2.0) {
			// doubleアーク
			g2.draw(new Arc2D.Double(x, y, w, h, start, extent, Arc2D.OPEN));
		} else {
			// intアーク
			g2.drawArc((int) x, (int) y, (int) w, (int) h, (int) start, (int) extent);
		}
	}

	private void sortMems() {
		Collections.sort(mems, new Comparator<VisualSrcMem>() {
			public int compare(VisualSrcMem n1, VisualSrcMem n2) {
				if (n1.depth < n2.depth) {
					return 1;
				} else if (n1.depth < n2.depth) {
					return -1;
				} else {
					return 0;
				}
			}
		});
	}

	Rectangle getDragRectangle() {
		Rectangle r = new Rectangle();
		Point sp = real2grid(display2real(startPoint));
		Point lp = real2grid(display2real(lastPoint));
		if (sp.x < lp.x) {
			r.x = sp.x;
			r.width = lp.x - sp.x;
		} else {
			r.x = lp.x;
			r.width = sp.x - lp.x;
		}
		if (sp.y < lp.y) {
			r.y = sp.y;
			r.height = lp.y - sp.y;
		} else {
			r.y = lp.y;
			r.height = sp.y - lp.y;
		}
		return r;
	}

	VisualSrcMem pickAMem(Point p) {
		Point2D.Double p2 = real2draw(display2real(p));
		VisualSrcMem pick = null;

		ArrayList<VisualSrcMem> rev = new ArrayList<VisualSrcMem>(mems);
		Collections.reverse(rev);

		for (VisualSrcMem mem : rev) {
			if (mem == null) {
				continue;
			}
			if (getMemShape(mem).contains(p2)) {
				pick = mem;
			}
		}
		return pick;
	}

	VisualSrcAtom pickAAtom(Point p) {
		Point2D.Double p2 = real2draw(display2real(p));
		VisualSrcAtom pick = null;

		ArrayList<VisualSrcAtom> rev = new ArrayList<VisualSrcAtom>(atoms);
		Collections.reverse(rev);

		for (VisualSrcAtom atom : rev) {
			if (atom == null) {
				continue;
			}
			if (getAtomShape(atom).contains(p2)) {
				pick = atom;
			}
		}
		return pick;
	}

	VisualSrcLink pickALink(Point p) {
		Point2D.Double p2 = real2draw(display2real(p));
		VisualSrcLink pick = null;

		ArrayList<VisualSrcLink> rev = new ArrayList<VisualSrcLink>(links);
		Collections.reverse(rev);

		for (VisualSrcLink link : rev) {
			if (link == null) {
				continue;
			}
			if (getLinkShape(link).ptSegDistSq(p2) < 10 * zoom) {
				pick = link;
			}
		}
		return pick;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void mousePressed(MouseEvent e) {
		requestFocus();

		if (visualPanel.toolBar.selected == visualPanel.toolBar.selectButton) {
			Point p = e.getPoint();

			VisualSrcAtom atom = pickAAtom(p);
			VisualSrcLink link = pickALink(p);
			VisualSrcMem mem = pickAMem(p);

			selectActioned = false;

			if (e.isControlDown()) {
				if (atom != null) {
					if (!selectAtoms.contains(atom)) {
						selectAtoms.add(atom);
						selectActioned = true;
					} else {
						selectAtoms.remove(atom);
					}
					selectLinks.clear();
					selectMems.clear();
				} else if (link != null) {
					if (!selectLinks.contains(link)) {
						selectLinks.add(link);
						selectActioned = true;
					} else {
						selectLinks.remove(link);
					}
					selectAtoms.clear();
					selectMems.clear();
				} else if (mem != null) {
					if (!selectMems.contains(mem)) {
						selectMems.add(mem);
						selectActioned = true;
					} else {
						selectMems.remove(mem);
					}
					selectAtoms.clear();
					selectLinks.clear();
				}
			} else {
				if (atom != null) {
					if (!selectAtoms.contains(atom)) {
						selectAtoms.clear();
						selectAtoms.add(atom);
					}
					selectLinks.clear();
					selectMems.clear();
					selectActioned = true;
				} else if (link != null) {
					if (!selectLinks.contains(link)) {
						selectLinks.clear();
						selectLinks.add(link);
					}
					selectAtoms.clear();
					selectMems.clear();
					selectActioned = true;
				} else if (mem != null) {
					if (!selectMems.contains(mem)) {
						selectMems.clear();
						selectMems.add(mem);
					}
					selectAtoms.clear();
					selectLinks.clear();
					selectActioned = true;
				} else {
					selectAtoms.clear();
					selectLinks.clear();
					selectMems.clear();
				}
			}
		}

		startPoint = e.getPoint();
		lastPoint = e.getPoint();
		update();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (lastPoint == null) {
			lastPoint = e.getPoint();
			return;
		}
		double dx = (double) (e.getX() - lastPoint.x) / zoom;
		double dy = (double) (e.getY() - lastPoint.y) / zoom;

		remainMoveX += dx;
		remainMoveY += dy;

		int gridMoveX = 0;
		int gridMoveY = 0;

		while (remainMoveX > gridInterval) {
			remainMoveX -= gridInterval;
			++gridMoveX;
		}
		while (remainMoveX < -gridInterval) {
			remainMoveX += gridInterval;
			--gridMoveX;
		}
		while (remainMoveY > gridInterval) {
			remainMoveY -= gridInterval;
			++gridMoveY;
		}
		while (remainMoveY < -gridInterval) {
			remainMoveY += gridInterval;
			--gridMoveY;
		}

		if (visualPanel.toolBar.selected == visualPanel.toolBar.selectButton) {
			if (selectActioned) {
				for (VisualSrcAtom atom : selectAtoms) {
					atom.move(gridMoveX, gridMoveY);
				}
				for (VisualSrcMem mem : selectMems) {
					mem.move(gridMoveX, gridMoveY);
				}
			} else {
				setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				displayX += dx;
				displayY += dy;
			}
		}

		if (startPoint == null) {
			startPoint = e.getPoint();
		}
		nowPoint = e.getPoint();
		lastPoint = e.getPoint();
		update();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (visualPanel.toolBar.selected == visualPanel.toolBar.atomButton) {
			if (nowPoint != null) {
				Point p = real2grid(display2real(nowPoint));
				if (!isOver(p)) {
					VisualSrcMem pmem = pickAMem(nowPoint);
					VisualSrcAtom atom = new VisualSrcAtom(p.x, p.y);
					if (pmem != null) {
						pmem.atoms.add(atom);
					}
					atoms.add(atom);
				}
			}
		} else if (visualPanel.toolBar.selected == visualPanel.toolBar.memButton) {
			if (startPoint != null) {
				Rectangle r = getDragRectangle();
				if (r.width * r.height > 0 && !isOver(r.x, r.y, r.width, r.height)) {
					VisualSrcMem pmem = pickAMem(nowPoint);
					VisualSrcMem mem = new VisualSrcMem(r.x, r.y, r.width, r.height, (pmem == null) ? 1 : (pmem.depth + 1));
					if (pmem != null) {
						pmem.mems.add(mem);
					}
					for (VisualSrcAtom ca : atoms) {
						if (getMemShape(mem).contains(grid2draw(ca.getPoint()))) {
							mem.atoms.add(ca);
						}
					}
					for (VisualSrcMem cm : mems) {
						if (getMemShape(mem).contains(grid2draw(cm.getPoint()))) {
							mem.mems.add(cm);
						}
					}
					mems.add(mem);
					sortMems();
				}
			}
		} else if (visualPanel.toolBar.selected == visualPanel.toolBar.linkButton) {
			if (startPoint != null) {
				Point p1 = real2grid(display2real(startPoint));
				Point p2 = real2grid(display2real(lastPoint));
				VisualSrcAtom atom1 = null;
				VisualSrcAtom atom2 = null;
				for (VisualSrcAtom atom : atoms) {
					if (p1.equals(atom.getPoint())) {
						atom1 = atom;
						break;
					}
				}
				for (VisualSrcAtom atom : atoms) {
					if (p2.equals(atom.getPoint())) {
						atom2 = atom;
						break;
					}
				}
				if (atom1 != null && atom2 != null) {
					VisualSrcLink link = new VisualSrcLink(atom1, atom2);
					atom1.links.add(link);
					atom2.links.add(link);
					links.add(link);
				}
			}
		}

		setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		lastPoint = null;
		startPoint = null;
		update();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		nowPoint = e.getPoint();
		update();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// TODO 自動生成されたメソッド・スタブ
		double z = Math.sqrt(zoom * 100);
		double dz = (double) e.getWheelRotation() / 2.0;
		if (e.isControlDown()) {
			dz /= 5;
		}
		z -= dz;
		setSliderZoom(z * z / 100.0);
		update();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			init();
			update();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO 自動生成されたメソッド・スタブ

	}
}
