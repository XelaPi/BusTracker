<?php
$response = array();

require_once __DIR__ . '/db_functions.php';

$db = new DB_FUNCTIONS();

$result = $db->getSchoolInfo($_POST["_id"]);

if ($result) {
	$response["success"] = 1;
	$response["_id"] = $result["_id"];
	$response["name"] = $result["name"];
	$response["rows"] = $result["rows"];
	$response["row_names"] = explode(";", $result["row_names"]);
	$response["default_row"] = $result["default_row"];
} else {
	$response["success"] = 0;
}

echo json_encode($response);
?>