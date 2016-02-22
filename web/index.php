<!doctype html>

<html lang="en">
<head>
	<meta charset="utf-8">

	<title>Bus Tracker</title>
	<meta name="description" content="See which school buses have arrived and where">
	<meta name="author" content="Alex Vanyo">
	
	<?php
	require_once __DIR__ . '/db_functions.php';

	$db = new DB_FUNCTIONS();
	?>
</head>

<body>
	<h1>Schools</h1>
	<?php
	$result = $db->getAllSchools();

	if ($result) {
		while ($school = mysql_fetch_array($result)) {
            echo "<a href=\"get_buses.php?_id=" . $school["_id"] . "\">" . $school["name"] . "</button>";
        }
	} else {
		echo "<p>Failed to load buses</p>";
	}
	?>
</body>
</html>