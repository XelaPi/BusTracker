<?php
$response = array();

require_once __DIR__ . '/db_functions.php';

$db = new DB_FUNCTIONS();

$result = $db->getAllSchools();

if (mysql_num_rows($result) > 0) {
	$response["success"] = 1;
	$response["schools"] = array();

	while ($row = mysql_fetch_array($result)) {
		$school = array();
		$school["_id"] = $row["_id"];
		$school["name"] = $row["name"];
		$school["rows"] = $row["rows"];
		$school["row_names"] = explode(";", $row["row_names"]);
		$school["default_row"] = $row["default_row"];
		array_push($response["schools"], $school);
	}

} else {
	$response["success"] = 0;
}

echo json_encode($response);
?>