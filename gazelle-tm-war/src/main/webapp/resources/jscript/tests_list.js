function colorTests(event) {
	colorRowsOfTable();
}

function colorRowsOfTable() {
	var table = document.getElementById("f:t");
	var numberOfRows = table.rows.length;

	for (var i = 1 ; i < numberOfRows; i++)
	{
		colorRow(table.rows[i]);
	}
}

function colorRow( idRow  )
{
	var currentRow = idRow  ;
	var tdToUse = currentRow.cells[2];
	if (tdToUse != null) {
		var selectToUse = tdToUse.firstChild.firstChild;
		if ( selectToUse != null )
		{
			if (selectToUse.value === '1') // ready
			currentRow.style.background="#CCFFCC" ;
			else if (selectToUse.value === '2') // deprecated
				currentRow.style.background="#F9675C" ;
			else if (selectToUse.value === '3') // to be completed
				currentRow.style.background="#FFCCCC" ;
			else if (selectToUse.value === '4') // storage/subtitute
				currentRow.style.background="##FFDC73" ;
			else if (selectToUse.value > '4' && selectToUse.value < '8') // other
				currentRow.style.background="#E0E0E0" ;
			else
				currentRow.style.background="#FFFFFF" ;
		}
	}
}

gazelleAddToBodyOnLoad(colorTests);

addAjaxStopFunction(function() {
	colorRowsOfTable();
});
