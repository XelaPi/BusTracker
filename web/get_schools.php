<?php
require_once __DIR__ . '/db_functions.php';

$db = new DB_FUNCTIONS();

echo json_encode($db->getAllSchools());
?>