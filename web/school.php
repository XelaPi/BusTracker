<?php
$school_id = $_GET["_id"];

if (!$school_id) {
	header("Location: index.php");
}
?>

<!doctype html>

<html lang="en">
<head>
	<meta charset="utf-8">
	<meta name="author" content="Alex Vanyo">
	<?php
	require_once __DIR__ . '/db_functions.php';

	$db = new DB_FUNCTIONS();

	$school_info = $db->getSchoolInfo($school_id);

	echo "<title>" . $school_info["name"] . "</title>"
	?>
	<meta name="description" content="See which school buses have arrived and where">
</head>
	<body>
		<h1>Schools</h1>
		<?php
		$result = $db->getAllBuses($school_id);
	
		if ($result["success"]) {
			foreach ($result["schools"] as $school) {
			echo "<a href=\"get_buses.php?_id=" . $school["_id"] . "\">" . $school["name"] . "</button>";
			}
		} else {
			echo "<p>Failed to load buses</p>";
		}
		?>
	</body>
</html>