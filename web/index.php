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
        echo "<ul>";
        while ($school = mysql_fetch_array($result)) {
            echo "<li><a href=\"school.php?_id=" . $school["_id"] . "\">" . $school["name"] . "</a></li>";
        }
        echo "</ul>";
    } else {
        echo "<p>Failed to load buses</p>";
    }
    ?>
    <p><a href="bustracker.apk">Get the Android app</a></p>
</body>
</html>