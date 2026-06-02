/*******************************************************************************
 * The contents of this file are subject to the Common Public Attribution License
 * Version 1.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.projectlibre.com/license . The License is based on the Mozilla Public
 * License Version 1.1 but Sections 14 and 15 have been added to cover use of
 * software over a computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be consistent
 * with Exhibit B.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the
 * specific language governing rights and limitations under the License. The
 * Original Code is ProjectLibre. The Original Developer is the Initial Developer
 * and is ProjectLibre Inc. All portions of the code written by ProjectLibre are
 * Copyright (c) 2012-2019. All Rights Reserved. All portions of the code written by
 * ProjectLibre are Copyright (c) 2012-2019. All Rights Reserved. Contributor
 * ProjectLibre, Inc.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * ProjectLibre End-User License Agreement (the ProjectLibre License) in which case
 * the provisions of the ProjectLibre License are applicable instead of those above.
 * If you wish to allow use of your version of this file only under the terms of the
 * ProjectLibre License and not to allow others to use your version of this file
 * under the CPAL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the ProjectLibre
 * License. If you do not delete the provisions above, a recipient may use your
 * version of this file under either the CPAL or the ProjectLibre Licenses.
 *
 * [NOTE: The text of this Exhibit A may differ slightly from the text of the notices
 * in the Source Code files of the Original Code. You should use the text of this
 * Exhibit A rather than the text found in the Original Code Source Code for Your
 * Modifications.]
 *
 * EXHIBIT B. Attribution Information for ProjectLibre required
 ************************************************************************/

package com.projectlibre1.util;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

import com.projectlibre1.graphic.configuration.BarFormat;
import com.projectlibre1.pm.scheduling.Schedule;

/**
 * Classic MS Project-inspired color palette for backward compatibility.
 */
public class ClassicMSProjectPalette implements GanttColorPalette {
    @Override
    public String getName() {
        return "Classic MS Project";
    }
    
    @Override
    public Color getChartBackground() {
        return Color.WHITE;
    }
    
    @Override
    public Color getGridLine() {
        return new Color(200, 200, 200); // Light gray
    }
    
    @Override
    public Color getTaskBar(Color statusColor) {
        // MS Project uses blue for most tasks
        if (statusColor != null) return statusColor;
        return new Color(100, 149, 237); // Cornflower blue
    }
    
    @Override
    public Color getStatusColor(Schedule schedule, Object context) {
        if (schedule == null) {
            return new Color(100, 149, 237); // Cornflower blue (default)
        }
        
        // MS Project cares only about completion, not "working/stuck"
        double percentComplete = schedule.getPercentComplete();
        if (percentComplete >= 0.99d) {
            return new Color(144, 238, 144); // Light green (completed)
        }
        return new Color(100, 149, 237); // Cornflower blue (default)
    }
    
    @Override
    public Color getAnnotationColor(BarFormat format) {
        return Color.BLACK;
    }

    @Override
    public Color getAccentColor(BarFormat format, Color statusColor, Object context) {
        return getTaskBar(statusColor);
    }

    @Override
    public Color getTextColor(Color fillColor) {
        return Color.BLACK;
    }

    @Override
    public Paint createBarPaint(Color fillColor, Rectangle2D bounds, boolean backgroundLayer, boolean textured) {
        Color barColor = getTaskBar(fillColor);
        if (!backgroundLayer) {
            return barColor;
        }
        return new Color(barColor.getRed(), barColor.getGreen(), barColor.getBlue(), 76);
    }

    @Override
    public Color getBaselineBarColor() {
        return new Color(0xA1A1A1);
    }

    @Override
    public Color getExternalLinkColor() {
        return Color.GRAY;
    }
    
    @Override
    public Color getDependencyLinkColor() {
        return Color.GRAY;
    }
    
    @Override
    public Color getProjectLineColor() {
        return Color.BLUE;
    }
    
    @Override
    public Color getStatusDateLineColor() {
        return Color.BLUE;
    }
}
