package com.projectlibre1.util;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.projectlibre1.graphic.configuration.BarFormat;
import com.projectlibre1.pm.assignment.Assignment;
import com.projectlibre1.pm.scheduling.Schedule;
import com.projectlibre1.pm.task.TaskSnapshot;
import com.projectlibre1.pm.task.Project;
import com.projectlibre1.pm.task.Task;

/**
 * Shared monday.com-inspired colors and painting helpers for Gantt rendering.
 */
public final class MondayGanttTheme {
	private static final int BACKGROUND_BAR_ALPHA = 76;
	private static final Color TEXT_DARK = new Color(0x202124);

	public static final Color DONE = new Color(0x00C875);
	public static final Color WORKING_ON_IT = new Color(0xFDAB3D);
	public static final Color STUCK = new Color(0xE2445C);
	public static final Color NOT_STARTED = new Color(0xC4C4C4);
	public static final Color BASELINE = new Color(0xA1A1A1);
	public static final Color NO_COMPARISON = new Color(0xE5E5E5);
	public static final Color GROUP_A = new Color(0x579BFC);
	public static final Color GROUP_B = new Color(0xA25DDC);
	public static final Color BACKGROUND = Color.WHITE;
	public static final Color HEADER_BACKGROUND = new Color(0xF5F6F8);
	public static final Color GRID_LINE = new Color(0xE1E1E1);

	private MondayGanttTheme() {
	}

	public static Color chartBackground() {
		return BACKGROUND;
	}

	public static Color headerBackground() {
		return HEADER_BACKGROUND;
	}

	public static Color gridLine() {
		return GRID_LINE;
	}

	public static Color projectLine() {
		return GROUP_A;
	}

	public static Color statusDateLine() {
		return GROUP_B;
	}

	public static Color externalLinkColor() {
		return GROUP_B;
	}

	public static Color statusColor(Schedule schedule, Object impl) {
		if (schedule == null)
			return GROUP_A;

		Color baselineColor = baselineComparisonColor(schedule, impl);
		if (baselineColor != null)
			return baselineColor;

		double percentComplete = clamp(schedule.getPercentComplete());
		if (percentComplete >= 1.0d)
			return DONE;
		if (isStuck(schedule, impl))
			return STUCK;
		if (percentComplete <= 0.0d)
			return NOT_STARTED;
		return WORKING_ON_IT;
	}

	public static Color accentColor(BarFormat format, Color statusColor) {
		if (format == null)
			return GROUP_A;

		String id = format.getId();
		if (id == null)
			return GROUP_A;

		if ("Bar.assignment".equals(id))
			return GROUP_B;
		if ("Bar.summary".equals(id))
			return GROUP_B;
		if ("Bar.deadline".equals(id))
			return GROUP_A;
		if ("Bar.baseline".equals(id) || id.startsWith("Bar.baseline"))
			return BASELINE;
		if ("Bar.totalSlack".equals(id))
			return GROUP_A;
		if ("Bar.critical".equals(id))
			return statusColor == null ? STUCK : shade(statusColor, 0.18f);
		if ("Link.link1".equals(id))
			return GROUP_A;
		if ("Bar.milestone".equals(id))
			return GROUP_A;
		if ("Bar.task".equals(id))
			return statusColor == null ? GROUP_A : shade(statusColor, 0.18f);
		return GROUP_A;
	}

	public static Paint createLayerPaint(Color baseColor, Rectangle2D bounds, boolean backgroundLayer) {
		if (bounds == null || baseColor == null)
			return baseColor;

		Color top = backgroundLayer ? withAlpha(soften(baseColor, 0.28f), BACKGROUND_BAR_ALPHA)
				: soften(baseColor, 0.18f);
		Color middle = backgroundLayer ? withAlpha(baseColor, BACKGROUND_BAR_ALPHA) : baseColor;
		Color bottom = backgroundLayer ? withAlpha(shade(baseColor, 0.10f), BACKGROUND_BAR_ALPHA)
				: shade(baseColor, 0.08f);
		return new LinearGradientPaint(
				new Point2D.Double(bounds.getX(), bounds.getY()),
				new Point2D.Double(bounds.getX(), bounds.getMaxY()),
				new float[] { 0.0f, 0.55f, 1.0f },
				new Color[] { top, middle, bottom });
	}

	public static Color withAlpha(Color color, int alpha) {
		if (color == null)
			return null;
		int clampedAlpha = Math.max(0, Math.min(255, alpha));
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), clampedAlpha);
	}

	public static Color textColorFor(Color fill) {
		return isDark(fill) ? Color.WHITE : TEXT_DARK;
	}

	public static boolean isDark(Color color) {
		if (color == null)
			return false;
		double luminance = (0.2126d * color.getRed() + 0.7152d * color.getGreen() + 0.0722d * color.getBlue()) / 255d;
		return luminance < 0.52d;
	}

	public static Color soften(Color color, float ratio) {
		return mix(color, Color.WHITE, ratio);
	}

	public static Color shade(Color color, float ratio) {
		return mix(color, Color.BLACK, ratio);
	}

	private static Color mix(Color color, Color target, float ratio) {
		if (color == null)
			return target;
		float weight = Math.max(0f, Math.min(1f, ratio));
		float targetWeight = 1f - weight;
		int red = Math.round(color.getRed() * targetWeight + target.getRed() * weight);
		int green = Math.round(color.getGreen() * targetWeight + target.getGreen() * weight);
		int blue = Math.round(color.getBlue() * targetWeight + target.getBlue() * weight);
		return new Color(red, green, blue, color.getAlpha());
	}

	private static boolean isStuck(Schedule schedule, Object impl) {
		if (schedule == null || schedule.getEnd() <= 0L)
			return false;
		if (clamp(schedule.getPercentComplete()) >= 1.0d)
			return false;
		if (isCritical(impl))
			return true;
		long statusDate = statusDateOf(impl);
		return statusDate > schedule.getEnd();
	}

	private static Color baselineComparisonColor(Schedule schedule, Object impl) {
		Task task = taskFrom(impl);
		if (task == null)
			return null;

		TaskSnapshot baseline = task.getBaselineSnapshot();
		if (baseline == null || baseline.getCurrentSchedule() == null)
			return null;

		long baselineFinish = baseline.getCurrentSchedule().getFinish();
		if (baselineFinish <= 0L)
			return NO_COMPARISON;
		return schedule.getEnd() > baselineFinish ? STUCK : DONE;
	}

	private static Task taskFrom(Object impl) {
		if (impl instanceof Task)
			return (Task)impl;
		if (impl instanceof Assignment)
			return ((Assignment)impl).getTask();
		return null;
	}

	private static boolean isCritical(Object impl) {
		if (impl instanceof Task)
			return ((Task)impl).isCritical();
		if (impl instanceof Assignment)
			return ((Assignment)impl).isCritical();
		return false;
	}

	private static long statusDateOf(Object impl) {
		if (impl instanceof Project) {
			long statusDate = ((Project)impl).getStatusDate();
			return statusDate == 0L ? System.currentTimeMillis() : statusDate;
		}
		if (impl instanceof Assignment) {
			Project project = ((Assignment)impl).getProject();
			if (project != null) {
				long statusDate = project.getStatusDate();
				return statusDate == 0L ? System.currentTimeMillis() : statusDate;
			}
		}
		if (impl instanceof Task) {
			Project project = ((Task)impl).getProject();
			if (project != null) {
				long statusDate = project.getStatusDate();
				return statusDate == 0L ? System.currentTimeMillis() : statusDate;
			}
		}
		return System.currentTimeMillis();
	}

	private static double clamp(double value) {
		if (value < 0.0d)
			return 0.0d;
		if (value > 1.0d)
			return 1.0d;
		return value;
	}
}
