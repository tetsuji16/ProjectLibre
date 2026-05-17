/*
 * file:       XlsxWriter.java
 * author:     ProjectLibre
 * copyright:  (c) ProjectLibre 2026
 * date:       2026-05-16
 */

/*
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307, USA.
 */

package net.sf.mpxj.xlsx;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.sf.mpxj.Duration;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.ProjectProperties;
import net.sf.mpxj.Relation;
import net.sf.mpxj.Resource;
import net.sf.mpxj.ResourceAssignment;
import net.sf.mpxj.Task;
import net.sf.mpxj.writer.AbstractProjectWriter;

/**
 * Writes a project plan to an Excel XLSX file.
 *
 * The XLSX file contains the following sheets:
 * <ul>
 *   <li><b>Project</b> - Project properties (key-value pairs)</li>
 *   <li><b>Tasks</b> - Task list with header row</li>
 *   <li><b>Resources</b> - Resource list with header row</li>
 *   <li><b>Assignments</b> - Resource assignments with header row</li>
 * </ul>
 *
 * This format is designed to be editable in both Microsoft Excel and ProjectLibre,
 * supporting concurrent editing via file locking on shared storage (OneDrive, NAS).
 */
public class XlsxWriter extends AbstractProjectWriter
{
   private static final String SHEET_PROJECT = "Project";
   private static final String SHEET_TASKS = "Tasks";
   private static final String SHEET_RESOURCES = "Resources";
   private static final String SHEET_ASSIGNMENTS = "Assignments";

   // Date format for Excel compatibility
   private static final String DATE_FORMAT = "yyyy-MM-dd";
   private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

   @Override
   public void write(ProjectFile projectFile, OutputStream outputStream) throws IOException
   {
      Workbook workbook = new XSSFWorkbook();
      CreationHelper creationHelper = workbook.getCreationHelper();

      // Create styles
      CellStyle headerStyle = createHeaderStyle(workbook);
      CellStyle dateStyle = createDateStyle(workbook, creationHelper);
      CellStyle numberStyle = workbook.createCellStyle();

      // Write sheets
      writeProjectSheet(workbook, projectFile, headerStyle);
      writeTasksSheet(workbook, projectFile, headerStyle, dateStyle, numberStyle);
      writeResourcesSheet(workbook, projectFile, headerStyle, numberStyle);
      writeAssignmentsSheet(workbook, projectFile, headerStyle, numberStyle);

      // Auto-size columns
      autoSizeColumns(workbook);

      // Write to output
      workbook.write(outputStream);
      workbook.close();
   }

   /**
    * Write the "Project" sheet with project properties.
    */
   private void writeProjectSheet(Workbook workbook, ProjectFile projectFile, CellStyle headerStyle)
   {
      Sheet sheet = workbook.createSheet(SHEET_PROJECT);
      ProjectProperties props = projectFile.getProjectProperties();

      int rowNum = 0;

      // Header
      Row headerRow = sheet.createRow(rowNum++);
      createStyledCell(headerRow, 0, "Property", headerStyle);
      createStyledCell(headerRow, 1, "Value", headerStyle);

      // Properties
      addPropertyRow(sheet, rowNum++, "Name", props.getProjectTitle());
      addPropertyRow(sheet, rowNum++, "StartDate", props.getStartDate());
      addPropertyRow(sheet, rowNum++, "StatusDate", props.getStatusDate());
      addPropertyRow(sheet, rowNum++, "Currency", props.getCurrencySymbol());
      addPropertyRow(sheet, rowNum++, "MinutesPerDay", props.getMinutesPerDay());
      addPropertyRow(sheet, rowNum++, "MinutesPerWeek", props.getMinutesPerWeek());
      addPropertyRow(sheet, rowNum++, "DaysPerMonth", props.getDaysPerMonth());
      addPropertyRow(sheet, rowNum++, "Author", props.getAuthor());
      addPropertyRow(sheet, rowNum++, "Company", props.getCompany());
      addPropertyRow(sheet, rowNum++, "CreationDate", props.getCreationDate());
      addPropertyRow(sheet, rowNum++, "LastSaved", props.getLastSaved());

      // Default task type
      if (props.getDefaultTaskType() != null)
      {
         addPropertyRow(sheet, rowNum++, "DefaultTaskType", props.getDefaultTaskType().toString());
      }

      // Calendar
      if (projectFile.getCalendars().size() > 0)
      {
         addPropertyRow(sheet, rowNum++, "CalendarName", projectFile.getCalendars().get(0).getName());
      }
   }

   /**
    * Write the "Tasks" sheet.
    */
   private void writeTasksSheet(Workbook workbook, ProjectFile projectFile, CellStyle headerStyle,
         CellStyle dateStyle, CellStyle numberStyle)
   {
      Sheet sheet = workbook.createSheet(SHEET_TASKS);
      int rowNum = 0;

      // Header row
      Row headerRow = sheet.createRow(rowNum++);
      String[] headers = {
         "ID", "UniqueID", "Name", "Duration", "DurationUnit",
         "Start", "Finish", "PercentComplete",
         "ActualStart", "ActualFinish", "ActualDuration", "RemainingDuration",
         "OutlineLevel", "Priority", "ConstraintType", "ConstraintDate",
         "Predecessors", "Notes", "Summary", "Milestone", "Critical",
         "Cost", "ActualCost", "Work", "ActualWork", "RemainingWork",
         "EarlyStart", "EarlyFinish", "LateStart", "LateFinish",
         "FreeSlack", "TotalSlack"
      };
      for (int i = 0; i < headers.length; i++)
      {
         createStyledCell(headerRow, i, headers[i], headerStyle);
      }

      // Task rows
      List<Task> tasks = projectFile.getTasks();
      for (Task task : tasks)
      {
         Row row = sheet.createRow(rowNum++);
         int col = 0;

         setCellValue(row, col++, task.getID());
         setCellValue(row, col++, task.getUniqueID());
         setCellValue(row, col++, task.getName());

         // Duration
         Duration duration = task.getDuration();
         if (duration != null)
         {
            setCellValue(row, col++, duration.getDuration());
            setCellValue(row, col++, duration.getUnits() != null ? duration.getUnits().toString() : "");
         }
         else
         {
            setCellValue(row, col++, (Double) null);
            setCellValue(row, col++, "");
         }

         // Dates
         setCellValue(row, col++, task.getStart(), dateStyle);
         setCellValue(row, col++, task.getFinish(), dateStyle);

         // Percent complete
         setCellValue(row, col++, task.getPercentageComplete());

         // Actual dates
         setCellValue(row, col++, task.getActualStart(), dateStyle);
         setCellValue(row, col++, task.getActualFinish(), dateStyle);

         // Actual duration
         Duration actualDur = task.getActualDuration();
         setCellValue(row, col++, actualDur != null ? actualDur.getDuration() : null);

         // Remaining duration
         Duration remainDur = task.getRemainingDuration();
         setCellValue(row, col++, remainDur != null ? remainDur.getDuration() : null);

         // Outline level
         setCellValue(row, col++, task.getOutlineLevel());

         // Priority
         setCellValue(row, col++, task.getPriority());

         // Constraint
         setCellValue(row, col++, task.getConstraintType() != null ? task.getConstraintType().toString() : "");
         setCellValue(row, col++, task.getConstraintDate(), dateStyle);

         // Predecessors
         setCellValue(row, col++, buildPredecessorString(task));

         // Notes
         setCellValue(row, col++, task.getNotes());

         // Boolean flags
         setCellValue(row, col++, task.getSummary());
         setCellValue(row, col++, task.getMilestone());
         setCellValue(row, col++, task.getCritical());

         // Cost
         setCellValue(row, col++, task.getCost());
         setCellValue(row, col++, task.getActualCost());

         // Work
         Duration work = task.getWork();
         setCellValue(row, col++, work != null ? formatDuration(work) : "");

         Duration actualWork = task.getActualWork();
         setCellValue(row, col++, actualWork != null ? formatDuration(actualWork) : "");

         Duration remainingWork = task.getRemainingWork();
         setCellValue(row, col++, remainingWork != null ? formatDuration(remainingWork) : "");

         // Schedule dates
         setCellValue(row, col++, task.getEarlyStart(), dateStyle);
         setCellValue(row, col++, task.getEarlyFinish(), dateStyle);
         setCellValue(row, col++, task.getLateStart(), dateStyle);
         setCellValue(row, col++, task.getLateFinish(), dateStyle);

         // Slack
         Duration freeSlack = task.getFreeSlack();
         setCellValue(row, col++, freeSlack != null ? formatDuration(freeSlack) : "");

         Duration totalSlack = task.getTotalSlack();
         setCellValue(row, col++, totalSlack != null ? formatDuration(totalSlack) : "");
      }
   }

   /**
    * Write the "Resources" sheet.
    */
   private void writeResourcesSheet(Workbook workbook, ProjectFile projectFile, CellStyle headerStyle,
         CellStyle numberStyle)
   {
      Sheet sheet = workbook.createSheet(SHEET_RESOURCES);
      int rowNum = 0;

      // Header row
      Row headerRow = sheet.createRow(rowNum++);
      String[] headers = {
         "ID", "UniqueID", "Name", "Type", "Initials",
         "Group", "EmailAddress", "Code", "MaxUnits",
         "StandardRate", "OvertimeRate", "CostPerUse"
      };
      for (int i = 0; i < headers.length; i++)
      {
         createStyledCell(headerRow, i, headers[i], headerStyle);
      }

      // Resource rows
      List<Resource> resources = projectFile.getResources();
      for (Resource resource : resources)
      {
         Row row = sheet.createRow(rowNum++);
         int col = 0;

         setCellValue(row, col++, resource.getID());
         setCellValue(row, col++, resource.getUniqueID());
         setCellValue(row, col++, resource.getName());
         setCellValue(row, col++, resource.getType() != null ? resource.getType().toString() : "");
         setCellValue(row, col++, resource.getInitials());
         setCellValue(row, col++, resource.getGroup());
         setCellValue(row, col++, resource.getEmailAddress());
         setCellValue(row, col++, resource.getCode());
         setCellValue(row, col++, resource.getMaxUnits());
         setCellValue(row, col++, resource.getStandardRate());
         setCellValue(row, col++, resource.getOvertimeRate());
         setCellValue(row, col++, resource.getCostPerUse());
      }
   }

   /**
    * Write the "Assignments" sheet.
    */
   private void writeAssignmentsSheet(Workbook workbook, ProjectFile projectFile, CellStyle headerStyle,
         CellStyle numberStyle)
   {
      Sheet sheet = workbook.createSheet(SHEET_ASSIGNMENTS);
      int rowNum = 0;

      // Header row
      Row headerRow = sheet.createRow(rowNum++);
      String[] headers = {
         "ID", "TaskID", "TaskUniqueID", "ResourceID", "ResourceUniqueID",
         "Units", "Work", "ActualWork", "RemainingWork",
         "Cost", "ActualCost", "RemainingCost"
      };
      for (int i = 0; i < headers.length; i++)
      {
         createStyledCell(headerRow, i, headers[i], headerStyle);
      }

      // Assignment rows
      List<Task> tasks = projectFile.getTasks();
      for (Task task : tasks)
      {
         List<ResourceAssignment> assignments = task.getResourceAssignments();
         for (ResourceAssignment assignment : assignments)
         {
            Row row = sheet.createRow(rowNum++);
            int col = 0;

            setCellValue(row, col++, assignment.getUniqueID());
            setCellValue(row, col++, assignment.getTask() != null ? assignment.getTask().getID() : null);
            setCellValue(row, col++, assignment.getTask() != null ? assignment.getTask().getUniqueID() : null);
            setCellValue(row, col++, assignment.getResource() != null ? assignment.getResource().getID() : null);
            setCellValue(row, col++, assignment.getResource() != null ? assignment.getResource().getUniqueID() : null);
            setCellValue(row, col++, assignment.getUnits());

            // Work
            Duration work = assignment.getWork();
            setCellValue(row, col++, work != null ? formatDuration(work) : "");

            Duration actualWork = assignment.getActualWork();
            setCellValue(row, col++, actualWork != null ? formatDuration(actualWork) : "");

            Duration remainingWork = assignment.getRemainingWork();
            setCellValue(row, col++, remainingWork != null ? formatDuration(remainingWork) : "");

            // Cost
            setCellValue(row, col++, assignment.getCost());
            setCellValue(row, col++, assignment.getActualCost());
            setCellValue(row, col++, assignment.getRemainingCost());
         }
      }
   }

   // --- Helper methods ---

   /**
    * Build a predecessor string for a task (e.g., "1FS,2SS+2d").
    */
   private String buildPredecessorString(Task task)
   {
      List<Relation> predecessors = task.getPredecessors();
      if (predecessors == null || predecessors.isEmpty())
      {
         return "";
      }

      StringBuilder sb = new StringBuilder();
      boolean first = true;
      for (Relation rel : predecessors)
      {
         if (!first)
         {
            sb.append(",");
         }
         first = false;

         Task target = rel.getTargetTask();
         if (target != null)
         {
            sb.append(target.getID());
         }

         if (rel.getType() != null)
         {
            switch (rel.getType())
            {
               case FINISH_START:
                  sb.append("FS");
                  break;
               case START_START:
                  sb.append("SS");
                  break;
               case FINISH_FINISH:
                  sb.append("FF");
                  break;
               case START_FINISH:
                  sb.append("SF");
                  break;
            }
         }

         Duration lag = rel.getLag();
         if (lag != null && lag.getDuration() != 0)
         {
            if (lag.getDuration() > 0)
            {
               sb.append("+");
            }
            sb.append((int) lag.getDuration()).append("d");
         }
      }
      return sb.toString();
   }

   /**
    * Format a duration as a string (e.g., "5d", "40h").
    */
   private String formatDuration(Duration duration)
   {
      if (duration == null)
      {
         return "";
      }
      double value = duration.getDuration();
      String unit = "d";
      if (duration.getUnits() != null)
      {
         switch (duration.getUnits())
         {
            case DAYS:
               unit = "d";
               break;
            case HOURS:
               unit = "h";
               break;
            case WEEKS:
               unit = "w";
               break;
            case MINUTES:
               unit = "m";
               break;
            case MONTHS:
               unit = "mo";
               break;
            default:
               unit = "d";
         }
      }
      if (value == (long) value)
      {
         return (long) value + unit;
      }
      return value + unit;
   }

   /**
    * Add a property row to the Project sheet.
    */
   private void addPropertyRow(Sheet sheet, int rowNum, String key, Object value)
   {
      Row row = sheet.createRow(rowNum);
      row.createCell(0).setCellValue(key);
      if (value != null)
      {
         if (value instanceof Date)
         {
            SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
            row.createCell(1).setCellValue(sdf.format((Date) value));
         }
         else if (value instanceof Number)
         {
            row.createCell(1).setCellValue(((Number) value).doubleValue());
         }
         else
         {
            row.createCell(1).setCellValue(value.toString());
         }
      }
      else
      {
         row.createCell(1).setCellValue("");
      }
   }

   /**
    * Create a styled header cell.
    */
   private void createStyledCell(Row row, int colIndex, String value, CellStyle style)
   {
      org.apache.poi.ss.usermodel.Cell cell = row.createCell(colIndex);
      cell.setCellValue(value);
      cell.setCellStyle(style);
   }

   /**
    * Create header cell style (bold).
    */
   private CellStyle createHeaderStyle(Workbook workbook)
   {
      CellStyle style = workbook.createCellStyle();
      Font font = workbook.createFont();
      font.setBold(true);
      style.setFont(font);
      return style;
   }

   /**
    * Create date cell style.
    */
   private CellStyle createDateStyle(Workbook workbook, CreationHelper creationHelper)
   {
      CellStyle style = workbook.createCellStyle();
      style.setDataFormat(creationHelper.createDataFormat().getFormat(DATE_FORMAT));
      return style;
   }

    /**
     * Set cell value for various types.
     */
    private void setCellValue(Row row, int col, Object value)
    {
       org.apache.poi.ss.usermodel.Cell cell = row.createCell(col);
       if (value == null)
       {
          cell.setCellValue("");
       }
       else if (value instanceof Number)
       {
          cell.setCellValue(((Number) value).doubleValue());
       }
       else if (value instanceof Boolean)
       {
          cell.setCellValue((Boolean) value);
       }
       else
       {
          cell.setCellValue(value.toString());
       }
    }

    private void setCellValue(Row row, int col, Date value, CellStyle dateStyle)
    {
       if (value != null)
       {
          org.apache.poi.ss.usermodel.Cell cell = row.createCell(col);
          cell.setCellValue(value);
         cell.setCellStyle(dateStyle);
      }
      else
      {
         row.createCell(col).setCellValue("");
      }
   }

   /**
    * Auto-size all columns in all sheets.
    */
   private void autoSizeColumns(Workbook workbook)
   {
      for (int i = 0; i < workbook.getNumberOfSheets(); i++)
      {
         Sheet sheet = workbook.getSheetAt(i);
         if (sheet.getPhysicalNumberOfRows() > 0)
         {
            Row headerRow = sheet.getRow(0);
            if (headerRow != null)
            {
               for (int j = 0; j < headerRow.getLastCellNum(); j++)
               {
                  sheet.autoSizeColumn(j);
                  // Add some padding
                  int width = sheet.getColumnWidth(j);
                  if (width < 2560)
                  {
                     sheet.setColumnWidth(j, 2560); // minimum 10 chars
                  }
                  else if (width > 15000)
                  {
                     sheet.setColumnWidth(j, 15000); // max ~50 chars
                  }
               }
            }
         }
      }
   }
}
