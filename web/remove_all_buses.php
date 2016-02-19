<?php
$response = array();

require_once __DIR__ . '/db_functions.php';

$db = new DB_FUNCTIONS();
$id = $_POST["_id"];

if ($_POST["password"] == $db->getSchoolInfo($id)["password"]) {
	$result = $db->removeAllBuses($id);
	
	$response["success"] = 1;
	$response["message"] = "Buses Removed: " . $result;
} else {
	$response["success"] = 0;
	$response["message"] = "Invalid Password";
}

echo json_encode($response);
?>