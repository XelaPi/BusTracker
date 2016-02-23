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
		$buses = array();

		require_once __DIR__ . '/db_functions.php';
	
		$db = new DB_FUNCTIONS();

		$school_info = $db->getSchoolInfo($school_id);

		echo "<title>" . $school_info["name"] . "</title>"
		?>
		<meta name="description" content="See which school buses have arrived and where">
	</head>
	<body>
		<?php
		echo "<h1>" . $school_info["name"] . "</h1>";

		$result = $db->getAllBuses($school_id);

		if (mysql_num_rows($result) > 0) {
			echo "<table>";
			
			echo "<tr>";
			$row_names = explode(";", $school_info["row_names"]);

			for ($i = 0; $i < $school_info["rows"]; $i++) {
				$buses[$i] = array();

				echo "<th>" . $row_names[$i] . "</th>";
			}
			echo "</tr>";
			
			while ($bus = mysql_fetch_array($result)) {
				array_push($buses[$bus["row"]], $bus["number"]);
			}

			$largest_row = 0;

			foreach ($buses as $row) {
				if (count($row) > $largest_row) {
					$largest_row = count($row);
				}
			}

			for ($i = 0; $i < $largest_row; $i++) {
				echo "<tr>";

                foreach ($buses as $row) {
                    if ($row[$i]) {
                        echo "<td>" . $row[$i] . "</td>";
                    } else {
                        echo "<td/>";
                    }
                }

                echo "</tr>";
			}
			
			echo "</table>";
		} else {
			echo "<p>Failed to load buses</p>";
		}
		?>
	</body>
</html>