/*
 * file:       XlsxReader.java
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.sf.mpxj.ConstraintType;
import net.sf.mpxj.Duration;
import net.sf.mpxj.MPXJException;
import net.sf.mpxj.Priority;
import net.sf.mpxj.ProjectCalendar;
import net.sf.mpxj.ProjectFile;
import net.sf.mpxj.ProjectProperties;
import net.sf.mpxj.Rate;
import net.sf.mpxj.Relation;
import net.sf.mpxj.RelationType;
import net.sf.mpxj.Resource;
import net.sf.mpxj.ResourceAssignment;
import net.sf.mpxj.ResourceType;
import net.sf.mpxj.Task;
import net.sf.mpxj.TaskField;
import net.sf.mpxj.TimeUnit;
import net.sf.mpxj.listener.ProjectListener;
import net.sf.mpxj.reader.AbstractProjectReader;

/**
 * Reads a project plan from an Excel XLSX file.
 *
 * The XLSX file is expected to contain the following sheets:
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
public class XlsxReader extends AbstractProjectReader
{
   private static final String SHEET_PROJECT = "Project";
   private static final String SHEET_TASKS = "Tasks";
   private static final String SHEET_RESOURCES = "Resources";
   private static final String SHEET_ASSIGNMENTS = "Assignments";

   // Task column names
   private static final String COL_ID = "ID";
   private static final String COL_UNIQUE_ID = "UniqueID";
   private static final String COL_NAME = "Name";
   private static final String COL_DURATION = "Duration";
   private static final String COL_DURATION_UNIT = "DurationUnit";
   private static final String COL_START = "Start";
   private static final String COL_FINISH = "Finish";
   private static final String COL_PERCENT_COMPLETE = "PercentComplete";
   private static final String COL_ACTUAL_START = "ActualStart";
   private static final String COL_ACTUAL_FINISH = "ActualFinish";
   private static final String COL_ACTUAL_DURATION = "ActualDuration";
   private static final String COL_REMAINING_DURATION = "RemainingDuration";
   private static final String COL_OUTLINE_LEVEL = "OutlineLevel";
   private static final String COL_PRIORITY = "Priority";
   private static final String COL_CONSTRAINT_TYPE = "ConstraintType";
   private static final String COL_CONSTRAINT_DATE = "ConstraintDate";
   private static final String COL_PREDECESSORS = "Predecessors";
   private static final String COL_NOTES = "Notes";
   private static final String COL_SUMMARY = "Summary";
   private static final String COL_MILESTONE = "Milestone";
   private static final String COL_CRITICAL = "Critical";
   private static final String COL_COST = "Cost";
   private static final String COL_ACTUAL_COST = "ActualCost";
   private static final String COL_WORK = "Work";
   private static final String COL_ACTUAL_WORK = "ActualWork";
    private static final String COL_REMAINING_WORK = "RemainingWork";
    private static final String COL_REMAINING_COST = "RemainingCost";
   private static final String COL_EARLY_START = "EarlyStart";
   private static final String COL_EARLY_FINISH = "EarlyFinish";
   private static final String COL_LATE_START = "LateStart";
   private static final String COL_LATE_FINISH = "LateFinish";
   private static final String COL_FREE_SLACK = "FreeSlack";
   private static final String COL_TOTAL_SLACK = "TotalSlack";

   // Resource column names
   private static final String COL_TYPE = "Type";
   private static final String COL_INITIALS = "Initials";
   private static final String COL_GROUP = "Group";
   private static final String COL_EMAIL = "EmailAddress";
   private static final String COL_CODE = "Code";
   private static final String COL_MAX_UNITS = "MaxUnits";
   private static final String COL_STANDARD_RATE = "StandardRate";
   private static final String COL_OVERTIME_RATE = "OvertimeRate";
   private static final String COL_COST_PER_USE = "CostPerUse";

    // Assignment column names
    private static final String COL_TASK_ID = "TaskID";
    private static final String COL_TASK_UNIQUE_ID = "TaskUniqueID";
    private static final String COL_RESOURCE_ID = "ResourceID";
    private static final String COL_RESOURCE_UNIQUE_ID = "ResourceUniqueID";
    private static final String COL_UNITS = "Units";

   // Project property names
   private static final String PROP_NAME = "Name";
   private static final String PROP_START_DATE = "StartDate";
   private static final String PROP_STATUS_DATE = "StatusDate";
   private static final String PROP_CURRENCY = "Currency";
   private static final String PROP_MINUTES_PER_DAY = "MinutesPerDay";
   private static final String PROP_MINUTES_PER_WEEK = "MinutesPerWeek";
   private static final String PROP_DAYS_PER_MONTH = "DaysPerMonth";
   private static final String PROP_DEFAULT_TASK_TYPE = "DefaultTaskType";
   private static final String PROP_DEFAULT_FIXED_COST_ACCRUAL = "DefaultFixedCostAccrual";
   private static final String PROP_AUTHOR = "Author";
   private static final String PROP_COMPANY = "Company";
   private static final String PROP_CREATION_DATE = "CreationDate";
   private static final String PROP_LAST_SAVED = "LastSaved";
   private static final String PROP_CALENDAR_NAME = "CalendarName";

   private List<ProjectListener> m_projectListeners;

   @Override
   public void addProjectListener(ProjectListener listener)
   {
      if (m_projectListeners == null)
      {
         m_projectListeners = new ArrayList<ProjectListener>();
      }
      m_projectListeners.add(listener);
   }

   @Override
   public ProjectFile read(InputStream inputStream) throws MPXJException
   {
      try
      {
         Workbook workbook = new XSSFWorkbook(inputStream);
         ProjectFile projectFile = new ProjectFile();

         // Read project properties
         readProjectProperties(workbook, projectFile);

         // Read resources first (needed for assignments)
         Map<Integer, Resource> resourcesById = readResources(workbook, projectFile);

         // Read tasks
         Map<Integer, Task> tasksById = readTasks(workbook, projectFile);

         // Read assignments
         readAssignments(workbook, projectFile, tasksById, resourcesById);

         // Process deferred predecessor relationships
         processPredecessors(workbook, tasksById);

         // Update structure
         projectFile.updateStructure();
         projectFile.getProjectConfig().updateUniqueCounters();

         workbook.close();

         projectFile.getProjectProperties().setFileApplication("ProjectLibre");
         projectFile.getProjectProperties().setFileType("XLSX");

         return projectFile;
      }
      catch (Exception ex)
      {
         throw new MPXJException(MPXJException.READ_ERROR, ex);
      }
   }

   @Override
   public ProjectFile read(File file) throws MPXJException
   {
      try (FileInputStream fis = new FileInputStream(file))
      {
         return read(fis);
      }
      catch (Exception ex)
      {
         throw new MPXJException(MPXJException.READ_ERROR, ex);
      }
   }

   /**
    * Read project properties from the "Project" sheet.
    */
   private void readProjectProperties(Workbook workbook, ProjectFile projectFile)
   {
      Sheet sheet = workbook.getSheet(SHEET_PROJECT);
      if (sheet == null)
      {
         return;
      }

      ProjectProperties props = projectFile.getProjectProperties();
      Map<String, String> propMap = new HashMap<String, String>();

      for (Row row : sheet)
      {
         Cell keyCell = row.getCell(0);
         Cell valueCell = row.getCell(1);
         if (keyCell != null && valueCell != null)
         {
            String key = getCellStringValue(keyCell);
            String value = getCellStringValue(valueCell);
            if (key != null && value != null)
            {
               propMap.put(key, value);
            }
         }
      }

      // Apply properties
      if (propMap.containsKey(PROP_NAME))
      {
         props.setProjectTitle(propMap.get(PROP_NAME));
      }
      if (propMap.containsKey(PROP_START_DATE))
      {
         Date startDate = parseDate(propMap.get(PROP_START_DATE));
         if (startDate != null)
         {
            props.setStartDate(startDate);
         }
      }
      if (propMap.containsKey(PROP_STATUS_DATE))
      {
         Date statusDate = parseDate(propMap.get(PROP_STATUS_DATE));
         if (statusDate != null)
         {
            props.setStatusDate(statusDate);
         }
      }
      if (propMap.containsKey(PROP_CURRENCY))
      {
         props.setCurrencyCode(propMap.get(PROP_CURRENCY));
      }
      if (propMap.containsKey(PROP_MINUTES_PER_DAY))
      {
         try
         {
            props.setMinutesPerDay(Integer.parseInt(propMap.get(PROP_MINUTES_PER_DAY)));
         }
         catch (NumberFormatException e)
         {
            // ignore
         }
      }
      if (propMap.containsKey(PROP_MINUTES_PER_WEEK))
      {
         try
         {
            props.setMinutesPerWeek(Integer.parseInt(propMap.get(PROP_MINUTES_PER_WEEK)));
         }
         catch (NumberFormatException e)
         {
            // ignore
         }
      }
      if (propMap.containsKey(PROP_DAYS_PER_MONTH))
      {
         try
         {
            props.setDaysPerMonth(Integer.parseInt(propMap.get(PROP_DAYS_PER_MONTH)));
         }
         catch (NumberFormatException e)
         {
            // ignore
         }
      }
      if (propMap.containsKey(PROP_AUTHOR))
      {
         props.setAuthor(propMap.get(PROP_AUTHOR));
      }
      if (propMap.containsKey(PROP_COMPANY))
      {
         props.setCompany(propMap.get(PROP_COMPANY));
      }
   }

   /**
    * Read resources from the "Resources" sheet.
    */
   private Map<Integer, Resource> readResources(Workbook workbook, ProjectFile projectFile)
   {
      Map<Integer, Resource> resourcesById = new HashMap<Integer, Resource>();
      Sheet sheet = workbook.getSheet(SHEET_RESOURCES);
      if (sheet == null)
      {
         return resourcesById;
      }

      // Read header row
      Row headerRow = sheet.getRow(0);
      if (headerRow == null)
      {
         return resourcesById;
      }
      Map<String, Integer> columnIndex = buildColumnIndex(headerRow);

      // Read data rows
      for (int i = 1; i <= sheet.getLastRowNum(); i++)
      {
         Row row = sheet.getRow(i);
         if (row == null)
         {
            continue;
         }

         Resource resource = projectFile.addResource();
         Integer id = getCellIntegerValue(row, columnIndex.get(COL_ID));
         if (id != null)
         {
            resource.setID(id);
            resourcesById.put(id, resource);
         }

         Integer uniqueId = getCellIntegerValue(row, columnIndex.get(COL_UNIQUE_ID));
         if (uniqueId != null)
         {
            resource.setUniqueID(uniqueId);
         }

         String name = getCellStringValue(row, columnIndex.get(COL_NAME));
         if (name != null)
         {
            resource.setName(name);
         }

         String type = getCellStringValue(row, columnIndex.get(COL_TYPE));
         if (type != null)
         {
            resource.setType(parseResourceType(type));
         }

         String initials = getCellStringValue(row, columnIndex.get(COL_INITIALS));
         if (initials != null)
         {
            resource.setInitials(initials);
         }

         String group = getCellStringValue(row, columnIndex.get(COL_GROUP));
         if (group != null)
         {
            resource.setGroup(group);
         }

         String email = getCellStringValue(row, columnIndex.get(COL_EMAIL));
         if (email != null)
         {
            resource.setEmailAddress(email);
         }

         String code = getCellStringValue(row, columnIndex.get(COL_CODE));
         if (code != null)
         {
            resource.setCode(code);
         }

         Double maxUnits = getCellDoubleValue(row, columnIndex.get(COL_MAX_UNITS));
         if (maxUnits != null)
         {
            resource.setMaxUnits(maxUnits);
         }

          BigDecimal standardRate = getCellBigDecimalValue(row, columnIndex.get(COL_STANDARD_RATE));
          if (standardRate != null)
          {
             resource.setStandardRate(new Rate(standardRate, TimeUnit.HOURS));
          }

          BigDecimal overtimeRate = getCellBigDecimalValue(row, columnIndex.get(COL_OVERTIME_RATE));
          if (overtimeRate != null)
          {
             resource.setOvertimeRate(new Rate(overtimeRate, TimeUnit.HOURS));
          }

         BigDecimal costPerUse = getCellBigDecimalValue(row, columnIndex.get(COL_COST_PER_USE));
         if (costPerUse != null)
         {
            resource.setCostPerUse(costPerUse);
         }
      }

      return resourcesById;
   }

   /**
    * Read tasks from the "Tasks" sheet.
    */
   private Map<Integer, Task> readTasks(Workbook workbook, ProjectFile projectFile)
   {
      Map<Integer, Task> tasksById = new HashMap<Integer, Task>();
      Sheet sheet = workbook.getSheet(SHEET_TASKS);
      if (sheet == null)
      {
         return tasksById;
      }

      // Read header row
      Row headerRow = sheet.getRow(0);
      if (headerRow == null)
      {
         return tasksById;
      }
      Map<String, Integer> columnIndex = buildColumnIndex(headerRow);

      // Read data rows
      for (int i = 1; i <= sheet.getLastRowNum(); i++)
      {
         Row row = sheet.getRow(i);
         if (row == null)
         {
            continue;
         }

         Task task = projectFile.addTask();
         Integer id = getCellIntegerValue(row, columnIndex.get(COL_ID));
         if (id != null)
         {
            task.setID(id);
            tasksById.put(id, task);
         }

         Integer uniqueId = getCellIntegerValue(row, columnIndex.get(COL_UNIQUE_ID));
         if (uniqueId != null)
         {
            task.setUniqueID(uniqueId);
         }

         String name = getCellStringValue(row, columnIndex.get(COL_NAME));
         if (name != null)
         {
            task.setName(name);
         }

         // Duration
         String durationStr = getCellStringValue(row, columnIndex.get(COL_DURATION));
         if (durationStr != null)
         {
            Duration duration = parseDuration(durationStr, projectFile);
            if (duration != null)
            {
               task.setDuration(duration);
            }
         }

         // Start date
         Date start = getCellDateValue(row, columnIndex.get(COL_START));
         if (start != null)
         {
            task.setStart(start);
         }

         // Finish date
         Date finish = getCellDateValue(row, columnIndex.get(COL_FINISH));
         if (finish != null)
         {
            task.setFinish(finish);
         }

         // Percent complete
         Double percentComplete = getCellDoubleValue(row, columnIndex.get(COL_PERCENT_COMPLETE));
         if (percentComplete != null)
         {
            task.setPercentageComplete(percentComplete);
         }

         // Actual start
         Date actualStart = getCellDateValue(row, columnIndex.get(COL_ACTUAL_START));
         if (actualStart != null)
         {
            task.setActualStart(actualStart);
         }

         // Actual finish
         Date actualFinish = getCellDateValue(row, columnIndex.get(COL_ACTUAL_FINISH));
         if (actualFinish != null)
         {
            task.setActualFinish(actualFinish);
         }

         // Actual duration
         String actualDurStr = getCellStringValue(row, columnIndex.get(COL_ACTUAL_DURATION));
         if (actualDurStr != null)
         {
            Duration actualDur = parseDuration(actualDurStr, projectFile);
            if (actualDur != null)
            {
               task.setActualDuration(actualDur);
            }
         }

         // Remaining duration
         String remainDurStr = getCellStringValue(row, columnIndex.get(COL_REMAINING_DURATION));
         if (remainDurStr != null)
         {
            Duration remainDur = parseDuration(remainDurStr, projectFile);
            if (remainDur != null)
            {
               task.setRemainingDuration(remainDur);
            }
         }

         // Outline level
         Integer outlineLevel = getCellIntegerValue(row, columnIndex.get(COL_OUTLINE_LEVEL));
         if (outlineLevel != null)
         {
            task.setOutlineLevel(outlineLevel);
         }

          // Priority
          Integer priority = getCellIntegerValue(row, columnIndex.get(COL_PRIORITY));
          if (priority != null)
          {
             task.setPriority(Priority.getInstance(priority));
          }

         // Constraint type
         String constraintType = getCellStringValue(row, columnIndex.get(COL_CONSTRAINT_TYPE));
         if (constraintType != null)
         {
            task.setConstraintType(parseConstraintType(constraintType));
         }

         // Constraint date
         Date constraintDate = getCellDateValue(row, columnIndex.get(COL_CONSTRAINT_DATE));
         if (constraintDate != null)
         {
            task.setConstraintDate(constraintDate);
         }

         // Notes
         String notes = getCellStringValue(row, columnIndex.get(COL_NOTES));
         if (notes != null)
         {
            task.setNotes(notes);
         }

         // Boolean flags
         Boolean summary = getCellBooleanValue(row, columnIndex.get(COL_SUMMARY));
         if (summary != null)
         {
            task.setSummary(summary);
         }

         Boolean milestone = getCellBooleanValue(row, columnIndex.get(COL_MILESTONE));
         if (milestone != null)
         {
            task.setMilestone(milestone);
         }

         Boolean critical = getCellBooleanValue(row, columnIndex.get(COL_CRITICAL));
         if (critical != null)
         {
            task.setCritical(critical);
         }

         // Cost
         BigDecimal cost = getCellBigDecimalValue(row, columnIndex.get(COL_COST));
         if (cost != null)
         {
            task.setCost(cost);
         }

         // Actual cost
         BigDecimal actualCost = getCellBigDecimalValue(row, columnIndex.get(COL_ACTUAL_COST));
         if (actualCost != null)
         {
            task.setActualCost(actualCost);
         }

         // Work
         String workStr = getCellStringValue(row, columnIndex.get(COL_WORK));
         if (workStr != null)
         {
            Duration work = parseDuration(workStr, projectFile);
            if (work != null)
            {
               task.setWork(work);
            }
         }

         // Actual work
         String actualWorkStr = getCellStringValue(row, columnIndex.get(COL_ACTUAL_WORK));
         if (actualWorkStr != null)
         {
            Duration actualWork = parseDuration(actualWorkStr, projectFile);
            if (actualWork != null)
            {
               task.setActualWork(actualWork);
            }
         }

         // Remaining work
         String remainWorkStr = getCellStringValue(row, columnIndex.get(COL_REMAINING_WORK));
         if (remainWorkStr != null)
         {
            Duration remainWork = parseDuration(remainWorkStr, projectFile);
            if (remainWork != null)
            {
               task.setRemainingWork(remainWork);
            }
         }

         // Early start
         Date earlyStart = getCellDateValue(row, columnIndex.get(COL_EARLY_START));
         if (earlyStart != null)
         {
            task.setEarlyStart(earlyStart);
         }

         // Early finish
         Date earlyFinish = getCellDateValue(row, columnIndex.get(COL_EARLY_FINISH));
         if (earlyFinish != null)
         {
            task.setEarlyFinish(earlyFinish);
         }

         // Late start
         Date lateStart = getCellDateValue(row, columnIndex.get(COL_LATE_START));
         if (lateStart != null)
         {
            task.setLateStart(lateStart);
         }

         // Late finish
         Date lateFinish = getCellDateValue(row, columnIndex.get(COL_LATE_FINISH));
         if (lateFinish != null)
         {
            task.setLateFinish(lateFinish);
         }

         // Free slack
         String freeSlackStr = getCellStringValue(row, columnIndex.get(COL_FREE_SLACK));
         if (freeSlackStr != null)
         {
            Duration freeSlack = parseDuration(freeSlackStr, projectFile);
            if (freeSlack != null)
            {
               task.setFreeSlack(freeSlack);
            }
         }

         // Total slack
         String totalSlackStr = getCellStringValue(row, columnIndex.get(COL_TOTAL_SLACK));
         if (totalSlackStr != null)
         {
            Duration totalSlack = parseDuration(totalSlackStr, projectFile);
            if (totalSlack != null)
            {
               task.setTotalSlack(totalSlack);
            }
         }
      }

      return tasksById;
   }

   /**
    * Read assignments from the "Assignments" sheet.
    */
   private void readAssignments(Workbook workbook, ProjectFile projectFile,
         Map<Integer, Task> tasksById, Map<Integer, Resource> resourcesById)
   {
      Sheet sheet = workbook.getSheet(SHEET_ASSIGNMENTS);
      if (sheet == null)
      {
         return;
      }

      // Read header row
      Row headerRow = sheet.getRow(0);
      if (headerRow == null)
      {
         return;
      }
      Map<String, Integer> columnIndex = buildColumnIndex(headerRow);

      // Read data rows
      for (int i = 1; i <= sheet.getLastRowNum(); i++)
      {
         Row row = sheet.getRow(i);
         if (row == null)
         {
            continue;
         }

         // Find task by ID or unique ID
         Task task = null;
         Integer taskId = getCellIntegerValue(row, columnIndex.get(COL_TASK_ID));
         if (taskId != null && tasksById.containsKey(taskId))
         {
            task = tasksById.get(taskId);
         }
         else
         {
            Integer taskUniqueId = getCellIntegerValue(row, columnIndex.get(COL_TASK_UNIQUE_ID));
            if (taskUniqueId != null)
            {
               for (Task t : tasksById.values())
               {
                  if (t.getUniqueID() == taskUniqueId)
                  {
                     task = t;
                     break;
                  }
               }
            }
         }

         // Find resource by ID or unique ID
         Resource resource = null;
         Integer resourceId = getCellIntegerValue(row, columnIndex.get(COL_RESOURCE_ID));
         if (resourceId != null && resourcesById.containsKey(resourceId))
         {
            resource = resourcesById.get(resourceId);
         }
         else
         {
            Integer resourceUniqueId = getCellIntegerValue(row, columnIndex.get(COL_RESOURCE_UNIQUE_ID));
            if (resourceUniqueId != null)
            {
               for (Resource r : resourcesById.values())
               {
                  if (r.getUniqueID() == resourceUniqueId)
                  {
                     resource = r;
                     break;
                  }
               }
            }
         }

         if (task != null && resource != null)
         {
            ResourceAssignment assignment = task.addResourceAssignment(resource);

            Integer id = getCellIntegerValue(row, columnIndex.get(COL_ID));
            if (id != null)
            {
               assignment.setUniqueID(id);
            }

            Double units = getCellDoubleValue(row, columnIndex.get(COL_UNITS));
            if (units != null)
            {
               assignment.setUnits(units);
            }

            String workStr = getCellStringValue(row, columnIndex.get(COL_WORK));
            if (workStr != null)
            {
               Duration work = parseDuration(workStr, projectFile);
               if (work != null)
               {
                  assignment.setWork(work);
               }
            }

            String actualWorkStr = getCellStringValue(row, columnIndex.get(COL_ACTUAL_WORK));
            if (actualWorkStr != null)
            {
               Duration actualWork = parseDuration(actualWorkStr, projectFile);
               if (actualWork != null)
               {
                  assignment.setActualWork(actualWork);
               }
            }

            String remainWorkStr = getCellStringValue(row, columnIndex.get(COL_REMAINING_WORK));
            if (remainWorkStr != null)
            {
               Duration remainWork = parseDuration(remainWorkStr, projectFile);
               if (remainWork != null)
               {
                  assignment.setRemainingWork(remainWork);
               }
            }

            BigDecimal cost = getCellBigDecimalValue(row, columnIndex.get(COL_COST));
            if (cost != null)
            {
               assignment.setCost(cost);
            }

            BigDecimal actualCost = getCellBigDecimalValue(row, columnIndex.get(COL_ACTUAL_COST));
            if (actualCost != null)
            {
               assignment.setActualCost(actualCost);
            }

            String remainCostStr = getCellStringValue(row, columnIndex.get(COL_REMAINING_COST));
            if (remainCostStr != null)
            {
               try
               {
                  assignment.setRemainingCost(new BigDecimal(remainCostStr));
               }
               catch (NumberFormatException e)
               {
                  // ignore
               }
            }
         }
      }
   }

   /**
    * Process predecessor relationships stored in the Tasks sheet.
    */
   private void processPredecessors(Workbook workbook, Map<Integer, Task> tasksById)
   {
      Sheet sheet = workbook.getSheet(SHEET_TASKS);
      if (sheet == null)
      {
         return;
      }

      Row headerRow = sheet.getRow(0);
      if (headerRow == null)
      {
         return;
      }
      Map<String, Integer> columnIndex = buildColumnIndex(headerRow);
      Integer predColIdx = columnIndex.get(COL_PREDECESSORS);
      if (predColIdx == null)
      {
         return;
      }

      for (int i = 1; i <= sheet.getLastRowNum(); i++)
      {
         Row row = sheet.getRow(i);
         if (row == null)
         {
            continue;
         }

         Integer taskId = getCellIntegerValue(row, columnIndex.get(COL_ID));
         if (taskId == null || !tasksById.containsKey(taskId))
         {
            continue;
         }

         Task task = tasksById.get(taskId);
         String predStr = getCellStringValue(row, predColIdx);
         if (predStr == null || predStr.trim().isEmpty())
         {
            continue;
         }

         // Parse predecessor string: "1FS", "2SS+2d", "3FF-1d", etc.
         String[] preds = predStr.split(",");
         for (String pred : preds)
         {
            pred = pred.trim();
            if (pred.isEmpty())
            {
               continue;
            }

            try
            {
               parsePredecessor(pred, task, tasksById);
            }
            catch (Exception e)
            {
               // Skip invalid predecessor references
            }
         }
      }
   }

   /**
    * Parse a single predecessor string like "1FS", "2SS+2d", "3FF-1d".
    */
   private void parsePredecessor(String predStr, Task task, Map<Integer, Task> tasksById)
   {
      // Extract task ID (numeric prefix)
      int idx = 0;
      while (idx < predStr.length() && Character.isDigit(predStr.charAt(idx)))
      {
         idx++;
      }
      if (idx == 0)
      {
         return;
      }

      int predId = Integer.parseInt(predStr.substring(0, idx));
      Task predTask = tasksById.get(predId);
      if (predTask == null)
      {
         return;
      }

      String remainder = predStr.substring(idx);

      // Extract relationship type
      RelationType type = RelationType.FINISH_START; // default
      if (remainder.startsWith("FS"))
      {
         type = RelationType.FINISH_START;
         remainder = remainder.substring(2);
      }
      else if (remainder.startsWith("SS"))
      {
         type = RelationType.START_START;
         remainder = remainder.substring(2);
      }
      else if (remainder.startsWith("FF"))
      {
         type = RelationType.FINISH_FINISH;
         remainder = remainder.substring(2);
      }
      else if (remainder.startsWith("SF"))
      {
         type = RelationType.START_FINISH;
         remainder = remainder.substring(2);
      }

      // Extract lag
      Duration lag = Duration.getInstance(0, TimeUnit.DAYS);
      if (!remainder.isEmpty())
      {
         try
         {
            lag = Duration.getInstance(Double.parseDouble(remainder), TimeUnit.DAYS);
         }
         catch (NumberFormatException e)
         {
            // ignore lag
         }
      }

      task.addPredecessor(predTask, type, lag);
   }

   /**
    * Build a column index map from the header row.
    */
   private Map<String, Integer> buildColumnIndex(Row headerRow)
   {
      Map<String, Integer> columnIndex = new HashMap<String, Integer>();
      for (Cell cell : headerRow)
      {
         String header = getCellStringValue(cell);
         if (header != null)
         {
            columnIndex.put(header, cell.getColumnIndex());
         }
      }
      return columnIndex;
   }

   // --- Cell value helper methods ---

   private String getCellStringValue(Cell cell)
   {
      if (cell == null)
      {
         return null;
      }
      CellType type = CellType.forInt(cell.getCellType());
      if (type == CellType.STRING)
      {
         String val = cell.getStringCellValue();
         return (val == null || val.isEmpty()) ? null : val;
      }
      else if (type == CellType.NUMERIC)
      {
         if (DateUtil.isCellDateFormatted(cell))
         {
            return cell.getDateCellValue().toString();
         }
         double numVal = cell.getNumericCellValue();
         if (numVal == (long) numVal)
         {
            return String.valueOf((long) numVal);
         }
         return String.valueOf(numVal);
      }
      else if (type == CellType.BOOLEAN)
      {
         return String.valueOf(cell.getBooleanCellValue());
      }
      else if (type == CellType.FORMULA)
      {
         try
         {
            return String.valueOf(cell.getNumericCellValue());
         }
         catch (Exception e)
         {
            try
            {
               return cell.getStringCellValue();
            }
            catch (Exception e2)
            {
               return null;
            }
         }
      }
      return null;
   }

   private String getCellStringValue(Row row, Integer colIdx)
   {
      if (colIdx == null || row == null)
      {
         return null;
      }
      Cell cell = row.getCell(colIdx);
      return getCellStringValue(cell);
   }

   private Integer getCellIntegerValue(Row row, Integer colIdx)
   {
      if (colIdx == null || row == null)
      {
         return null;
      }
      Cell cell = row.getCell(colIdx);
      if (cell == null)
      {
         return null;
      }
      try
      {
         if (CellType.forInt(cell.getCellType()) == CellType.STRING)
         {
            String val = cell.getStringCellValue();
            if (val == null || val.isEmpty())
            {
               return null;
            }
            return Integer.parseInt(val.trim());
         }
         return (int) cell.getNumericCellValue();
      }
      catch (Exception e)
      {
         return null;
      }
   }

   private Double getCellDoubleValue(Row row, Integer colIdx)
   {
      if (colIdx == null || row == null)
      {
         return null;
      }
      Cell cell = row.getCell(colIdx);
      if (cell == null)
      {
         return null;
      }
      try
      {
         if (CellType.forInt(cell.getCellType()) == CellType.STRING)
         {
            String val = cell.getStringCellValue();
            if (val == null || val.isEmpty())
            {
               return null;
            }
            return Double.parseDouble(val.trim());
         }
         return cell.getNumericCellValue();
      }
      catch (Exception e)
      {
         return null;
      }
   }

   private BigDecimal getCellBigDecimalValue(Row row, Integer colIdx)
   {
      if (colIdx == null || row == null)
      {
         return null;
      }
      Cell cell = row.getCell(colIdx);
      if (cell == null)
      {
         return null;
      }
      try
      {
         if (CellType.forInt(cell.getCellType()) == CellType.STRING)
         {
            String val = cell.getStringCellValue();
            if (val == null || val.isEmpty())
            {
               return null;
            }
            return new BigDecimal(val.trim());
         }
         return BigDecimal.valueOf(cell.getNumericCellValue());
      }
      catch (Exception e)
      {
         return null;
      }
   }

   private Date getCellDateValue(Row row, Integer colIdx)
   {
      if (colIdx == null || row == null)
      {
         return null;
      }
      Cell cell = row.getCell(colIdx);
      if (cell == null)
      {
         return null;
      }
      try
      {
         if (CellType.forInt(cell.getCellType()) == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell))
         {
            return cell.getDateCellValue();
         }
         else if (CellType.forInt(cell.getCellType()) == CellType.STRING)
         {
            return parseDate(cell.getStringCellValue());
         }
      }
      catch (Exception e)
      {
         return null;
      }
      return null;
   }

   private Boolean getCellBooleanValue(Row row, Integer colIdx)
   {
      if (colIdx == null || row == null)
      {
         return null;
      }
      Cell cell = row.getCell(colIdx);
      if (cell == null)
      {
         return null;
      }
      try
      {
         if (CellType.forInt(cell.getCellType()) == CellType.BOOLEAN)
         {
            return cell.getBooleanCellValue();
         }
         else if (CellType.forInt(cell.getCellType()) == CellType.STRING)
         {
            String val = cell.getStringCellValue().trim().toLowerCase();
            return "true".equals(val) || "yes".equals(val) || "1".equals(val);
         }
         else if (CellType.forInt(cell.getCellType()) == CellType.NUMERIC)
         {
            return cell.getNumericCellValue() != 0;
         }
      }
      catch (Exception e)
      {
         return null;
      }
      return null;
   }

   // --- Parsing helper methods ---

   private static final String[] DATE_FORMATS = {
      "yyyy-MM-dd", "yyyy/MM/dd", "MM/dd/yyyy", "dd/MM/yyyy",
      "yyyy-MM-dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss",
      "MM/dd/yyyy HH:mm:ss", "dd/MM/yyyy HH:mm:ss",
      "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ssXXX"
   };

   private Date parseDate(String dateStr)
   {
      if (dateStr == null || dateStr.trim().isEmpty())
      {
         return null;
      }
      dateStr = dateStr.trim();
      for (String format : DATE_FORMATS)
      {
         try
         {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setLenient(false);
            return sdf.parse(dateStr);
         }
         catch (ParseException e)
         {
            // try next format
         }
      }
      return null;
   }

   private Duration parseDuration(String durationStr, ProjectFile projectFile)
   {
      if (durationStr == null || durationStr.trim().isEmpty())
      {
         return null;
      }
      durationStr = durationStr.trim();

      // Try to parse formats like "5d", "40h", "2w", "3d 4h", "5.5d"
      try
      {
         // Simple numeric value (assumed days)
         double value = Double.parseDouble(durationStr);
         return Duration.getInstance(value, TimeUnit.DAYS);
      }
      catch (NumberFormatException e)
      {
         // Try to parse with unit suffix
         String lower = durationStr.toLowerCase();
         double value;
         TimeUnit unit = TimeUnit.DAYS;

         if (lower.endsWith("d"))
         {
            unit = TimeUnit.DAYS;
            value = Double.parseDouble(lower.substring(0, lower.length() - 1).trim());
         }
         else if (lower.endsWith("h"))
         {
            unit = TimeUnit.HOURS;
            value = Double.parseDouble(lower.substring(0, lower.length() - 1).trim());
         }
         else if (lower.endsWith("w"))
         {
            unit = TimeUnit.WEEKS;
            value = Double.parseDouble(lower.substring(0, lower.length() - 1).trim());
         }
         else if (lower.endsWith("m"))
         {
            unit = TimeUnit.MINUTES;
            value = Double.parseDouble(lower.substring(0, lower.length() - 1).trim());
         }
         else if (lower.endsWith("mo"))
         {
            unit = TimeUnit.MONTHS;
            value = Double.parseDouble(lower.substring(0, lower.length() - 2).trim());
         }
         else
         {
            return null;
         }

         return Duration.getInstance(value, unit);
      }
   }

   private ResourceType parseResourceType(String typeStr)
   {
      if (typeStr == null)
      {
         return ResourceType.WORK;
      }
      String lower = typeStr.trim().toLowerCase();
      if ("work".equals(lower) || "w".equals(lower))
      {
         return ResourceType.WORK;
      }
      else if ("material".equals(lower) || "m".equals(lower))
      {
         return ResourceType.MATERIAL;
      }
      else if ("cost".equals(lower) || "c".equals(lower))
      {
         return ResourceType.COST;
      }
      return ResourceType.WORK;
   }

   private ConstraintType parseConstraintType(String typeStr)
   {
      if (typeStr == null)
      {
         return null;
      }
      String upper = typeStr.trim().toUpperCase();
      try
      {
         return ConstraintType.valueOf(upper);
      }
      catch (IllegalArgumentException e)
      {
         // Try common aliases
         if ("ASAP".equals(upper))
         {
            return ConstraintType.AS_SOON_AS_POSSIBLE;
         }
         else if ("ALAP".equals(upper))
         {
            return ConstraintType.AS_LATE_AS_POSSIBLE;
         }
         return null;
      }
   }
}
