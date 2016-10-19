<?php
$response = array();

require_once __DIR__ . '/db_functions.php';

$db = new DB_FUNCTIONS();
$id = $_POST["_id"];
$row = $_POST["bus_row"];
$number = $_POST["bus_number"];

if ($_POST["password"] == $db->getSchoolInfo($id)["password"]) {
    $result = $db->removeBus($id, $row, $number);

    $response["success"] = 1;
    $response["message"] = "Bus Removed: " . $result;
} else {
    $response["success"] = 0;
    $response["message"] = "Invalid Password";
}

echo json_encode($response);
?>