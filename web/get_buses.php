<?php
$response = array();

require_once __DIR__ . '/db_functions.php';

$db = new DB_FUNCTIONS();

$result = $db->getAllBuses($_GET["_id"]);

if (mysql_num_rows($result) > 0) {
	$response["success"] = 1;
	$response["buses"] = array();
 
	while ($row = mysql_fetch_array($result)) {
		$bus = array();
		$bus["bus_number"] = $row["number"];
		$bus["bus_row"] = $row["row"];
		array_push($response["buses"], $bus);
	}
} else {
	$response["success"] = 0;
}

echo json_encode($response);
?>