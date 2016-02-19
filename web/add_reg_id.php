<?php
$response = array();

require_once __DIR__ . '/db_functions.php';
$db = new DB_FUNCTIONS();

$gcmRegID = $_POST["reg_id"]; 
$result = $db->addRegId($gcmRegID);

if ($result) {
	$response["success"] = 1;
} else {
    $response["success"] = 0;
}

echo json_encode($response);
?>