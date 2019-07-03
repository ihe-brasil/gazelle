/*
 * Copyright 2015 IHE International (http://www.ihe.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package net.ihe.gazelle.tm.gazelletest.action;

import net.ihe.gazelle.common.filter.FilterDataModel;
import net.ihe.gazelle.tm.gazelletest.model.instance.MonitorInSession;
import net.ihe.gazelle.tm.systems.model.TestingSession;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <b>Class Description : </b>MonitorExporter<br>
 * <br>
 *
 * @author Anne-Gaelle Berge / IHE-Europe development Project
 * @version 1.0 - 08/10/15
 * @class MonitorExporter
 * @package net.ihe.gazelle.tm.gazelletest.action
 * @see anne-gaelle.berge@ihe-europe.net - http://gazelle.ihe.net
 */
@Name("monitorExporter")
@Scope(ScopeType.EVENT)
@BypassInterceptors
public class MonitorExporter implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(MonitorExporter.class);
    private static final long serialVersionUID = 8762484565412904606L;
    private CellStyle style;
    private int colIndex;

    @SuppressWarnings("unchecked")
    public void exportDataModel(FilterDataModel<MonitorInSession> monitors) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("exportDataModel");
        }
        export((List<MonitorInSession>) monitors.getAllItems(FacesContext.getCurrentInstance()));
    }

    public void export(List<MonitorInSession> monitors) throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("export");
        }
        TestingSession testingSession = TestingSession.getSelectedTestingSession();
        // initiate workbook
        Workbook workbook = new HSSFWorkbook();

        Sheet sheet = workbook.createSheet();

        style = workbook.createCellStyle();
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());

        int rowIndex = 0;
        Row row = sheet.createRow(rowIndex);
        rowIndex++;

        // columns header
        colIndex = 0;
        String[] headers = {"Institution", "Username", "Last name", "First name", "Email", "is blocked", "is activated", "# tests"};
        for (String headTitle : headers) {
            addCell(row, headTitle);
        }
        row.setRowStyle(style);
        // increase row index
        rowIndex++;
        // rows for monitors
        for (MonitorInSession monitorInSession : monitors) {
            row = sheet.createRow(rowIndex);
            colIndex = 0;
            addCell(row, monitorInSession.getUser().getInstitution().getKeyword());
            addCell(row, monitorInSession.getUser().getUsername());
            addCell(row, monitorInSession.getUser().getLastname());
            addCell(row, monitorInSession.getUser().getFirstname());
            addCell(row, monitorInSession.getUser().getEmail());
            addCell(row, monitorInSession.getUser().getBlocked().toString());
            addCell(row, monitorInSession.getUser().getActivated().toString());
            if (monitorInSession.getTestList() != null) {
                addCell(row, Integer.toString(monitorInSession.getTestList().size()));
            } else {
                addCell(row, "0");
            }
            row.setRowStyle(style);
            rowIndex++;
        }

        // resize cells
        for (int columnIndex = 0; columnIndex < colIndex; columnIndex++) {
            sheet.autoSizeColumn(columnIndex);
        }

        CellReference start = new CellReference(0, 0);
        String startString = start.formatAsString();
        CellReference end = new CellReference(rowIndex - 1, colIndex - 1);
        String endString = end.formatAsString();
        sheet.setAutoFilter(CellRangeAddress.valueOf(startString + ":" + endString));

        redirectExport(workbook, "Monitors_" + testingSession.getDescription());
    }

    private void addCell(Row row, Object value) {
        Cell cell = row.createCell(colIndex);
        if (value != null) {
            Class<? extends Object> valueClass = value.getClass();
            if (String.class.isAssignableFrom(valueClass)) {
                cell.setCellValue((String) value);
            } else if (Date.class.isAssignableFrom(valueClass)) {
                cell.setCellValue((Date) value);
            }
        }
        cell.setCellStyle(style);
        colIndex++;
    }

    private void redirectExport(Workbook wb, String title) throws IOException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();

        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
        ServletOutputStream servletOutputStream = response.getOutputStream();
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename=\"" + title + ".xls\"");
        wb.write(servletOutputStream);
        servletOutputStream.flush();
        servletOutputStream.close();
        facesContext.responseComplete();
    }
}
