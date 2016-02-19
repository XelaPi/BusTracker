<?php
$response = array();

require_once __DIR__ . '/db_functions.php';

$db = new DB_FUNCTIONS();

$result = $db->getSchoolInfo($_POST["_id"]);

$response["success"] = $_POST["password"] == $result["password"] ? 1 : 0;

echo json_encode($response);
?>