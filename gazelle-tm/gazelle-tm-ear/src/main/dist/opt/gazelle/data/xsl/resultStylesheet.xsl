<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output encoding="UTF-8" indent="yes" method="html" media-type="text/html"/>
    <xd:doc xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl" scope="stylesheet">
        <xd:desc>
            <xd:p><xd:b>Created on:</xd:b> Jul 5, 2010</xd:p>
            <xd:p><xd:b>Modified on:</xd:b> Aug 24, 2010</xd:p>
            <xd:p><xd:b>Modified on:</xd:b> June 30, 2011</xd:p>
            <xd:p><xd:b>Author:</xd:b> Abderrazek Boufahja, Anne-Gaëlle BERGE, IHE Development, KEREVAL Rennes</xd:p>
            <xd:p></xd:p>
        </xd:desc>
    </xd:doc>
    <xsl:template match="/">
        <script type="text/javascript">
                    function hideOrViewValidationDetails() {
                       var detaileddiv = document.getElementById('resultdetailedann');
                       if (detaileddiv != null){
                           var onn = document.getElementById('resultdetailedann_switch_on');
                           if (onn != null){
                               if (onn.style.display == 'block') onn.style.display = 'none';
                               else if (onn.style.display == 'none') onn.style.display = 'block';
                           }
                           var off = document.getElementById('resultdetailedann_switch_off');
                           if (off != null){
                               if (off.style.display == 'block') off.style.display = 'none';
                               else if (off.style.display == 'none') off.style.display = 'block';
                           }
                           var body = document.getElementById('resultdetailedann_body');
                           if (body != null){
                               if (body.style.display == 'block') body.style.display = 'none';
                               else if (body.style.display == 'none') body.style.display = 'block';
                           }
                       }
                    }

                    function hideOrUnhide(elem){
                        var elemToHide = document.getElementById(elem.name + '_sch');
                        if (elemToHide != null){
                                if (elem.checked){
                                    elemToHide.style.display = 'none';
                                }
                                else{
                                    elemToHide.style.display = 'block';
                                }
                           }
                    }


                </script>
                <h2>Schematron-based Validation Report</h2>
                <br/>
                <!-- general information -->
                <div class="rich-panel styleResultBackground">
                    <div class="rich-panel-header">General Information</div>
                    <div class="rich-panel-body">
                        <table border="0">
                            <tr>
                                <td><b>Validation Date</b></td>
                                <td><xsl:value-of select="detailedResult/ValidationResultsOverview/ValidationDate"/> - <xsl:value-of select="detailedResult/ValidationResultsOverview/ValidationTime"/></td> 
                            </tr>
                            <tr>
                                <td><b>Validation Service</b></td>
                                <td><xsl:value-of select="detailedResult/ValidationResultsOverview/ValidationServiceName"/> (<xsl:value-of select="detailedResult/ValidationResultsOverview/ValidationServiceVersion"/>)</td>         
                            </tr>
                            <xsl:if test="count(detailedResult/ValidationResultsOverview/Schematron) = 1">
                                <tr>
                                    <td>
                                        <b>Schematron</b>
                                    </td>
                                    <td>
                                        <xsl:value-of select="detailedResult/ValidationResultsOverview/Schematron"/>
                                    </td>
                                </tr>
                            </xsl:if>
                            <tr>
                                <td><b>Validation Test Status</b></td>
                                <td>
                                    <xsl:if test="contains(detailedResult/ValidationResultsOverview/ValidationTestResult, 'PASSED')">
                                        <div class="PASSED"><xsl:value-of select="detailedResult/ValidationResultsOverview/ValidationTestResult"/></div>
                                    </xsl:if>
                                    <xsl:if test="contains(detailedResult/ValidationResultsOverview/ValidationTestResult, 'FAILED')">
                                        <div class="FAILED"><xsl:value-of select="detailedResult/ValidationResultsOverview/ValidationTestResult"/></div>
                                    </xsl:if>
                                    <xsl:if test="contains(detailedResult/ValidationResultsOverview/ValidationTestResult, 'ABORTED')">
                                        <div class="ABORTED"><xsl:value-of select="detailedResult/ValidationResultsOverview/ValidationTestResult"/></div>
                                    </xsl:if>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>
                <br/>
                <!-- overview -->
                <div class="rich-panel styleResultBackground">
                    <div class="rich-panel-header">Result Overview</div>
                    <div class="rich-panel-body">
                        <table border="0">
                            <xsl:if test="count(detailedResult/DocumentWellFormed) = 1">
                                <tr>
                                    <td>
                                        <b><a href="#wellformed">XML</a></b> (well-formed)
                                    </td>
                                    <td>                                    
                                        <xsl:choose>
                                            <xsl:when test="contains(detailedResult/DocumentWellFormed/Result, 'PASSED')">
                                                <div class="PASSED">
                                                    PASSED
                                                </div>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <div class="FAILED">
                                                    FAILED
                                                </div>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                </tr>
                            </xsl:if>
                            <xsl:if test="count(detailedResult/DocumentValidCDA) = 1">
                                <tr>
                                    <td>
                                        <b><a href="#xsd">CDA</a></b> (<a href="http://gazelle.ihe.net/xsd/CDA.xsd">CDA.xsd</a>)
                                    </td>
                                    <td>                                    
                                        <xsl:choose>
                                            <xsl:when test="contains(detailedResult/DocumentValidCDA/Result, 'PASSED')">
                                                <div class="PASSED">
                                                    PASSED
                                                </div>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <div class="FAILED">
                                                    FAILED
                                                </div>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>    
                                </tr>
                            </xsl:if>
                            <xsl:if test="count(detailedResult/DocumentValidEpsos) = 1">
                                <tr>
                                    <td>
                                        <b><a href="#xsd">epSOS</a></b> (<a href="http://gazelle.ihe.net/xsd/CDA_extended.xsd">CDA_extended.xsd</a>)
                                    </td>
                                    <td>                                    
                                        <xsl:choose>
                                            <xsl:when test="contains(detailedResult/DocumentValidEpsos/Result, 'PASSED')">
                                                <div class="PASSED">
                                                    PASSED
                                                </div>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <div class="FAILED">
                                                    FAILED
                                                </div>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>    
                                </tr>
                            </xsl:if>
                            <xsl:if test="count(detailedResult/DocumentValid) = 1">
                                <tr>
                                    <td>
                                        <b><a href="#xsd">XSD</a></b>
                                    </td>
                                    <td>                                    
                                        <xsl:choose>
                                            <xsl:when test="contains(detailedResult/DocumentValid/Result, 'PASSED')">
                                                <div class="PASSED">
                                                    PASSED
                                                </div>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <div class="FAILED">
                                                    FAILED
                                                </div>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>    
                                </tr>
                            </xsl:if>
                            <xsl:if test="count(detailedResult/MIFValidation) = 1">
                                <tr>
                                    <td>
                                        <b><a href="#mif">MIF</a></b>
                                    </td>
                                    <td>
                                        <xsl:choose>
                                            <xsl:when test="contains(detailedResult/MIFValidation/Result, 'PASSED')">
                                                <div class="PASSED">
                                                    PASSED
                                                </div>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <div class="FAILED">
                                                    FAILED
                                                </div>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                </tr>
                            </xsl:if>
                            <xsl:if test="count(detailedResult/SchematronValidation) = 1">
                                <tr>
                                    <td>
                                        <b><a href="#schematron">Schematron</a></b>
                                    </td>
                                    <td>
                                        <xsl:if test="contains(detailedResult/SchematronValidation/Result, 'PASSED')">
                                            <div class="PASSED">
                                                <xsl:value-of select="detailedResult/SchematronValidation/Result"/>
                                            </div>
                                        </xsl:if>
                                        <xsl:if test="contains(detailedResult/SchematronValidation/Result, 'FAILED')">
                                            <div class="FAILED">
                                                <xsl:value-of select="detailedResult/SchematronValidation/Result"/>
                                            </div>
                                        </xsl:if>
                                        <xsl:if test="contains(detailedResult/SchematronValidation/Result, 'ABORTED')">
                                            <div class="ABORTED">
                                                <xsl:value-of select="detailedResult/SchematronValidation/Result"/>
                                            </div>
                                        </xsl:if>
                                    </td>
                                </tr>
                            </xsl:if>
                        </table> 
                    </div>
                </div>
                <br/>    
                <!-- document well-formed -->                
                <xsl:if test="count(detailedResult/DocumentWellFormed) = 1">
                    <div class="rich-panel styleResultBackground" id="wellformed">
                        <div class="rich-panel-header">XML Validation</div>
                        <div class="rich-panel-body">
                            <xsl:choose>
                                <xsl:when test="detailedResult/DocumentWellFormed/Result = 'PASSED'">
                                    <p class="PASSED">The XML document is well-formed</p>                        
                                </xsl:when>
                                <xsl:otherwise>
                                    <p class="FAILED">The XML document is not well-formed</p>
                                </xsl:otherwise>
                            </xsl:choose>
                        </div>
                    </div>
                    <br/>
                </xsl:if>
                <!-- document valid XSD -->
                <xsl:if test="count(detailedResult/DocumentValidCDA) = 1 or count(detailedResult/DocumentValidEpsos) = 1 or count(detailedResult/DocumentValid) = 1">
                    <div class="rich-panel styleResultBackground" id="xsd">
                        <div class="rich-panel-header">XSD Validation</div>
                        <div class="rich-panel-body">
                            <xsl:if test="count(detailedResult/DocumentValidCDA) = 1">
                                <xsl:choose>
                                    <xsl:when test="detailedResult/DocumentValidCDA/Result = 'PASSED'">
                                        <p class="PASSED">The XML document is a valid CDA regarding HL7 specifications</p>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <p class="FAILED">The XML document is not a valid CDA regarding HL7 specifications because of the following reasons: </p>
                                        <xsl:if test="count(detailedResult/DocumentValidCDA/*) &gt; 3">
                                            <ul>
                                                <xsl:for-each select="detailedResult/DocumentValidCDA/*">
                                                    <xsl:if test="contains(current(), 'error')">
                                                        <li><xsl:value-of select="current()"/></li>
                                                    </xsl:if>
                                                </xsl:for-each>
                                            </ul>
                                        </xsl:if>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:if>
                            <xsl:if test="count(detailedResult/DocumentValidEpsos) = 1">
                                <xsl:choose>
                                    <xsl:when test="detailedResult/DocumentValidEpsos/Result = 'PASSED'">
                                        <p class="PASSED">The XML document is a valid CDA regarding epSOS specifications</p>                           
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <p class="FAILED">The XML document is not a valid CDA regarding epSOS specifications because of the following reasons: </p>
                                        <xsl:if test="count(detailedResult/DocumentValidEpsos/*) &gt; 3">
                                            <ul>
                                                <xsl:for-each select="detailedResult/DocumentValidEpsos/*">
                                                    <xsl:if test="contains(current(), 'error')">
                                                        <li><xsl:value-of select="current()"/></li>
                                                    </xsl:if>
                                                </xsl:for-each>
                                            </ul>
                                        </xsl:if>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:if>
                            <xsl:if test="count(detailedResult/DocumentValid) = 1">
                                <xsl:choose>
                                    <xsl:when test="detailedResult/DocumentValid/Result = 'PASSED'">
                                        <p class="PASSED">The XML document is valid</p>                        
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <p class="FAILED">The XML document is not valid because of the following reasons: </p>
                                        <xsl:if test="count(detailedResult/DocumentValid/*) &gt; 3">
                                            <ul>
                                                <xsl:for-each select="detailedResult/DocumentValid/*">
                                                    <xsl:if test="contains(current(), 'error')">
                                                        <li><xsl:value-of select="current()"/></li>
                                                    </xsl:if>
                                                </xsl:for-each>
                                            </ul>
                                        </xsl:if>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:if>
                        </div>
                    </div>
                </xsl:if>
                <!-- MIF Validation -->
                <br/>
                <xsl:if test="count(detailedResult/MIFValidation) = 1">
                    <div class="rich-panel styleResultBackround" id="mif">
                        <div class="rich-panel-header">MIF Validation</div>
                        <div class="rich-panel-body">
                            <table>
                                <tr>    
                                    <td>    
                                        <b>Result</b>   
                                    </td>
                                    <td>    
                                        <xsl:choose>
                                            <xsl:when test="contains(detailedResult/MIFValidation/Result, 'PASSED')">
                                                <div class="PASSED">
                                                    PASSED
                                                </div>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <div class="FAILED">
                                                    FAILED
                                                </div>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                </tr>
                                <tr>
                                    <td valign="top">
                                        <b>Summary</b>
                                    </td>
                                    <td>
                                        <xsl:choose>
                                            <xsl:when test="detailedResult/MIFValidation/ValidationCounters/NrOfValidationErrors &gt; 0">
                                                <a href="#miferrors"><xsl:value-of select="detailedResult/MIFValidation/ValidationCounters/NrOfValidationErrors"/> error(s)</a>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                No error
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <br/>
                                        <xsl:choose>
                                            <xsl:when test="detailedResult/MIFValidation/ValidationCounters/NrOfValidationWarnings &gt; 0">
                                                <a href="#mifwarnings"><xsl:value-of select="detailedResult/MIFValidation/ValidationCounters/NrOfValidationWarnings"/> warning(s)</a>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                No warning
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <br/>
                                        <xsl:choose>
                                            <xsl:when test="detailedResult/MIFValidation/ValidationCounters/NrOfValidationNotes &gt; 0">
                                                <a href="#mifinfo"><xsl:value-of select="detailedResult/MIFValidation/ValidationCounters/NrOfValidationInfos"/> info(s)</a>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                No info
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        
                                    </td>
                                </tr>
                            </table>
                            <xsl:if test="count(detailedResult/MIFValidation/Error) &gt; 0">
                                <p id="miferrors">Errors</p>
                                <xsl:for-each select="detailedResult/MIFValidation/Error">
                                    
                                    <table class="Error" width="98%">
                                        <tr>
                                            <td valign="top" width="100"><b>Location</b></td>
                                            <td align="left"><xsl:value-of select="@location"/> ( Line: <xsl:value-of select="@startLine"/>, Column: <xsl:value-of select="@startColumn"/>)</td>
                                        </tr>
                                        <tr>
                                            <td valign="top"><b>Description</b></td>
                                            <td align="left"><xsl:value-of select="@message"/></td>
                                        </tr>
                                    </table>
                                    <br/>
                                </xsl:for-each>
                            </xsl:if>
                            <xsl:if test="count(detailedResult/MIFValidation/Warning) &gt; 0">
                                <p id="mifwarnings">Warnings</p>
                                <xsl:for-each select="detailedResult/MIFValidation/Warning">
                                    <table class="Warning" width="98%">
                                        <tr>
                                            <td valign="top" width="100"><b>Location</b></td>
                                            <td><xsl:value-of select="@location"/> ( Line: <xsl:value-of select="@startLine"/>, Column: <xsl:value-of select="@startColumn"/>)</td>
                                        </tr>
                                        <tr>
                                            <td valign="top"><b>Description</b></td>
                                            <td><xsl:value-of select="@message"/></td>
                                        </tr>
                                    </table>
                                    <br/>
                                </xsl:for-each>
                            </xsl:if>
                            <xsl:if test="count(detailedResult/MIFValidation/Info) &gt; 0">
                                <p id="mifinfos">Infos</p>
                                <xsl:for-each select="detailedResult/MIFValidation/Info">
                                    
                                    <table class="Note" width="98%">
                                        <tr>
                                            <td valign="top" width="100"><b>Location</b></td>
                                            <td><xsl:value-of select="@location"/> ( Line: <xsl:value-of select="@startLine"/>, Column: <xsl:value-of select="@startColumn"/>)</td>
                                        </tr>
                                        <tr>
                                            <td valign="top"><b>Description</b></td>
                                            <td><xsl:value-of select="@message"/></td>
                                        </tr>
                                    </table>
                                    <br/>
                                </xsl:for-each>
                            </xsl:if>
                            <xsl:if test="count(detailedResult/MIFValidation/Problem) &gt; 0">
                                <p>Other problems</p>
                                <xsl:for-each select="detailedResult/MIFValidation/Problem">
                                    
                                    <table class="Unkown" width="98%">
                                        <tr>
                                            <td valign="top" width="100"><b>Location</b></td>
                                            <td><xsl:value-of select="@location"/> ( Line: <xsl:value-of select="@startLine"/> - Column: <xsl:value-of select="@startColumn"/>)</td>
                                        </tr>
                                        <tr>
                                            <td valign="top"><b>Description</b></td>
                                            <td><xsl:value-of select="@message"/></td>
                                        </tr>
                                        
                                    </table>
                                    <br/>                                    
                                </xsl:for-each>
                            </xsl:if>
                            
                        </div>
                    </div>
                </xsl:if>
                <!-- Schematron Validation -->
                <br/>
                <xsl:if test="count(detailedResult/SchematronValidation) = 1">
                    <div class="rich-panel styleResultBackground" id="schematron">
                        <div class="rich-panel-header">SchematronValidation</div>
                        <div class="rich-panel-body">
                            <table>
                                <tr>    
                                    <td>    
                                        <b>Result</b>   
                                    </td>
                                    <td>    
                                        <xsl:choose>
                                            <xsl:when test="contains(detailedResult/SchematronValidation/Result, 'PASSED')">
                                                <div class="PASSED">
                                                    PASSED
                                                </div>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <div class="FAILED">
                                                    FAILED
                                                </div>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                </tr>
                                <tr>
                                    <td valign="top">
                                        <b>Summary</b>
                                    </td>
                                    <td>
                                        <xsl:choose>
                                            <xsl:when test="detailedResult/SchematronValidation/ValidationCounters/NrOfChecks &gt; 0">
                                                <xsl:value-of select="detailedResult/SchematronValidation/ValidationCounters/NrOfChecks"/> check(s) performed
                                            </xsl:when>
                                            <xsl:otherwise>
                                                No check was performed
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <br/>
                                        <xsl:choose>
                                            <xsl:when test="detailedResult/SchematronValidation/ValidationCounters/NrOfValidationErrors &gt; 0">
                                                <a href="#errors"><xsl:value-of select="detailedResult/SchematronValidation/ValidationCounters/NrOfValidationErrors"/> error(s)</a>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                No error
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <br/>
                                        <xsl:choose>
                                            <xsl:when test="detailedResult/SchematronValidation/ValidationCounters/NrOfValidationWarnings &gt; 0">
                                                <a href="#warnings"><xsl:value-of select="detailedResult/SchematronValidation/ValidationCounters/NrOfValidationWarnings"/> warning(s)</a>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                No warning
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <br/>
                                        <xsl:choose>
                                            <xsl:when test="detailedResult/SchematronValidation/ValidationCounters/NrOfValidationNotes &gt; 0">
                                                <a href="#notes"><xsl:value-of select="detailedResult/SchematronValidation/ValidationCounters/NrOfValidationNotes"/> note(s)</a>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                No note
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <br/>
                                        <xsl:choose>
                                            <xsl:when test="detailedResult/SchematronValidation/ValidationCounters/NrOfValidationReports &gt; 0">
                                                <a href="#reports"><xsl:value-of select="detailedResult/SchematronValidation/ValidationCounters/NrOfValidationReports"/> successful check(s)</a>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                No successful check
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <br/>
                                        <xsl:choose>
                                            <xsl:when test="detailedResult/SchematronValidation/ValidationCounters/NrOfValidationUnknown &gt; 0">
                                                <a href="#unknown"><xsl:value-of select="detailedResult/SchematronValidation/ValidationCounters/NrOfValidationUnknown"/> unknown exception(s)</a>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                No unknown exception
                                            </xsl:otherwise>
                                        </xsl:choose>                                        
                                    </td>
                                </tr>
                            </table>
			 <b>HIDE : </b>
                        <input type="checkbox" onclick="hideOrUnhide(this)" name="Errors">Errors</input>
                        <input type="checkbox" onclick="hideOrUnhide(this)" name="Warnings">Warnings</input>
                        <input type="checkbox" onclick="hideOrUnhide(this)" name="Notes">Notes</input>
                        <input type="checkbox" onclick="hideOrUnhide(this)" name="Reports">Reports</input>

                            <xsl:if test="detailedResult/SchematronValidation/ValidationCounters/NrOfValidationErrors &gt; 0">
                            <div id="Errors_sch">
                                <p id="errors"><b>Errors</b></p>
                                <xsl:for-each select="detailedResult/SchematronValidation/Error">
                                    <table class="Error" width="98%">
                                        <tr>
                                            <td valign="top" width="100"><b>Test</b></td>
                                            <td><xsl:value-of select="Test"/></td>
                                        </tr>
                                        <tr>
                                            <td valign="top"><b>Location</b></td>
                                            <td><xsl:value-of select="Location"/></td>
                                        </tr>
                                        <tr>
                                            <td valign="top"><b>Description</b></td>
                                            <td><xsl:value-of select="Description"/></td>
                                        </tr>
                                    </table>
                                    <br/>
                                </xsl:for-each>
				</div>
                            </xsl:if>
                            <xsl:if test="detailedResult/SchematronValidation/ValidationCounters/NrOfValidationWarnings &gt; 0">
                            <div id="Warnings_sch">
                                <p id="warnings"><b>Warnings</b></p>
                                <xsl:for-each select="detailedResult/SchematronValidation/Warning">
                                    <table class="Warning" width="98%">
                                        <tr>
                                            <td valign="top" width="100"><b>Test</b></td>
                                            <td><xsl:value-of select="Test"/></td>
                                        </tr>
                                        <tr>
                                            <td valign="top"><b>Location</b></td>
                                            <td><xsl:value-of select="Location"/></td>
                                        </tr>
                                        <tr>
                                            <td valign="top"><b>Description</b></td>
                                            <td><xsl:value-of select="Description"/></td>
                                        </tr>
                                    </table>
                                    <br/>
                                </xsl:for-each>
				</div>
                            </xsl:if>
                            <xsl:if test="detailedResult/SchematronValidation/ValidationCounters/NrOfValidationNotes &gt; 0">
                            <div id="Notes_sch">
                                <p id="notes"><b>Notes</b></p>
                                <xsl:for-each select="detailedResult/SchematronValidation/Note">
                                    <table class="Note" width="98%">
                                        <tr>
                                            <td valign="top" width="100"><b>Test</b></td>
                                            <td><xsl:value-of select="Test"/></td>
                                        </tr>
                                        <tr>
                                            <td valign="top"><b>Location</b></td>
                                            <td><xsl:value-of select="Location"/></td>
                                        </tr>
                                        <tr>
                                            <td valign="top"><b>Description</b></td>
                                            <td><xsl:value-of select="Description"/></td>
                                        </tr>
                                    </table>
                                    <br/>
                                </xsl:for-each>
			</div>
                            </xsl:if>
                            <xsl:if test="detailedResult/SchematronValidation/ValidationCounters/NrOfValidationUnknown &gt; 0">
                                <p id="unknown"><b>Unknown exceptions</b></p>
                                <xsl:for-each select="detailedResult/SchematronValidation/Unknown">
                                    <table class="Unknown" width="98%">
                                        <tr>
                                            <td valign="top" width="100"><b>Test</b></td>
                                            <td><xsl:value-of select="Test"/></td>
                                        </tr>
                                        <tr>
                                            <td valign="top"><b>Location</b></td>
                                            <td><xsl:value-of select="Location"/></td>
                                        </tr>
                                        <tr>
                                            <td valign="top"><b>Description</b></td>
                                            <td><xsl:value-of select="Description"/></td>
                                        </tr>
                                    </table>
                                    <br/>
                                </xsl:for-each>
                            </xsl:if>
                            <xsl:if test="detailedResult/SchematronValidation/ValidationCounters/NrOfValidationReports &gt; 0">
                            <div id="Reports_sch">
                                <p id="reports"><b>Reports</b></p>
                                <xsl:for-each select="detailedResult/SchematronValidation/Report">
                                    <table class="Report" width="98%">
                                        <tr>
                                            <td valign="top" width="100"><b>Test</b></td>
                                            <td><xsl:value-of select="Test"/></td>
                                        </tr>
                                        <tr>
                                            <td valign="top"><b>Location</b></td>
                                            <td><xsl:value-of select="Location"/></td>
                                        </tr>
                                        <tr>
                                            <td valign="top"><b>Description</b></td>
                                            <td><xsl:value-of select="Description"/></td>
                                        </tr>
                                    </table>
                                    <br/>
                                </xsl:for-each>
				</div>
                            </xsl:if>
                        </div>
                    </div>
                </xsl:if>
    </xsl:template>
</xsl:stylesheet>
