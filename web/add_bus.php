<?php
$response = array();

require_once __DIR__ . '/db_functions.php';

$db = new DB_FUNCTIONS();
$id = $_POST["_id"];
$number = $_POST["bus_number"];
$row = $_POST["bus_row"];

if ($_POST["password"] == $db->getSchoolInfo($id)["password"]) {
    if (isset($row)) {
        $result = $db->addBusWithRow($id, $row, $number);
    } else {
        $result = $db->addBus($id, $number);
    }

    $response["success"] = 1;
    $response["message"] = "Bus Added: " . $result;
} else {
    $response["success"] = 0;
    $response["message"] = "Invalid Password";
}

echo json_encode($response);
?>