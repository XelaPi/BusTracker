<?php
	
	require_once __DIR__ . '/db_functions.php';
	$db = new DB_FUNCTIONS();
	
	$pushStatus = "GCM Status Message will appear here";	
	if(!empty($_GET["push"])) {
		$number = $_POST["bus_number"];
		$row = $_POST["bus_row"];
		
		if (empty($row)) {
			$pushStatus = $db->addBus(1, $number);
		} else {
			$pushStatus = $db->addBusWithRow(1, $row, $number);
		}
	}
	
	if(!empty($_GET["delete"])) {
		$pushStatus = $db->removeAllBuses();
	}
?>

<html>
    <head>
        <title>BusTracker PHP</title>
		<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
		<script>
		$(function(){
			$("textarea").val("");
		});
		function checkTextAreaLen(){
			var msgLength = $.trim($("textarea").val()).length;
			if (msgLength == 0) {
				alert("Please enter message before hitting submit button");
				return false;
			} else {
				return true;
			}
		}
		</script>
    </head>
	<body>
		<div id="formdiv">
		<h1>Bus Messaging</h1>	
		<form method="post" action="gcm.php?push=true" onsubmit="return checkTextAreaLen()">
			<select name="bus_row">
				<option value="">Select Row (optional)</option>
				<option value="0">Diagonal Row</option>
				<option value="1">Straight Row</option>
			</select>
			<textarea name="bus_number" placeholder="Bus Number"></textarea>
			<br/>
			<input type="submit"  value="Add Bus"/>
		</form>
		<form method="post" action="gcm.php?delete=true">					                                                      
				<input type="submit"  value="Remove All Buses"/>
		</form>
		</div>
		<p id="status">
		<?php echo $pushStatus; ?>
		</p>        
    </body>
</html>