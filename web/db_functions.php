<?php

//header('Content-Type: text/event-stream');
//header('Cache-Control: no-cache');

class DB_FUNCTIONS {

	private $db;
	
	function __construct() {
		require_once __DIR__ . '/db_connect.php';

		$this->db = new DB_CONNECT();
	}

	function __destruct() {

	}

	function sendMessage($message) {
		$fields = array(
			'registration_ids' => $this->getAllRegIds(),
			'data' => $message,
		);
		$fields = json_encode($fields);
		$arrContextOptions = array(
			"http" => array(
				"method" => "POST",
				"header" =>
					'Authorization: key = AIzaSyD2TpBIlbAdT0F2y-eWIUwOVq2Y_neaVpM'. "\r\n" .
					'Content-Type: application/json'. "\r\n",
				"content" => $fields,
			),
		);
		$arrContextOptions = stream_context_create($arrContextOptions);
		$result = file_get_contents('https://gcm-http.googleapis.com/gcm/send', false, $arrContextOptions);

	    //echo $fields;
	    //flush();

		return $result;
	}

	public function getRow($id, $number) {
		$query = sprintf("SELECT * FROM bus_rows WHERE school_id='%s' AND number='%s'", mysql_real_escape_string($id), mysql_real_escape_string($number));

		$result = mysql_query($query);
		if ($result) {
			while ($row = mysql_fetch_array($result, MYSQL_ASSOC)) {
				return $row['row'];
			}
	}
		return 0;
	}

	public function addRegId($regId) {
		$query = sprintf("INSERT INTO reg_ids (reg_id) VALUES('%s')", mysql_real_escape_string($regId));

		$result = mysql_query($query);
		return $result;
	}

	public function getAllRegIds() {
		$query = sprintf("SELECT * FROM reg_ids");

		$result = mysql_query($query);

		$gcmRegIds = array();

		while ($row = mysql_fetch_array($result, MYSQL_ASSOC)) {
			array_push($gcmRegIds, $row["reg_id"]);
		}

		return $gcmRegIds;
	}

	public function addBus($id, $number) {
		$row = $this->getRow($id, $number);

		return $this->addBusWithRow($id, $row, $number);
	}

	public function addBusWithRow($id, $row, $number) {
		$query = sprintf("INSERT INTO buses (school_id, row, number) VALUES('%s', '%s', '%s')", mysql_real_escape_string($id), mysql_real_escape_string($row), mysql_real_escape_string($number));

		$result = mysql_query($query);

		if ($result) {

		$sql_row = $this->getSchoolInfo($id);
		$row_name= explode(";", $sql_row["row_names"])[$row];
		$position = $this->getBusPosition($id, $row, $number);

			$message = array(
				"message" => "add_bus",
				"_id" => $id,
				"bus_row" => $row,
				"row_name" => $row_name,
				"bus_number" => $number,
				"position" => $position
			);
			$response = $this->sendMessage($message);
		}

		return $response;
	}

	public function getAllBuses($id) {
		$query = sprintf("SELECT * FROM buses WHERE school_id='%s' ORDER BY _id", mysql_real_escape_string($id));

		$result = mysql_query($query);

		return $result;
	}

	public function getBusPosition($id, $row, $number) {
		$query = sprintf("SELECT COUNT(*)+1 FROM buses WHERE _id<(SELECT _id FROM buses WHERE school_id='%1\$s' AND row='%2\$s' AND number='%3\$s') AND school_id='%1\$s' AND row='%2\$s'",
				mysql_real_escape_string($id),
				mysql_real_escape_string($row),
				mysql_real_escape_string($number));

		$result = mysql_fetch_assoc(mysql_query($query))["COUNT(*)+1"];
		return $result;
	}

	public function removeAllBuses($id) {
		$query = sprintf("DELETE FROM buses WHERE school_id='%s'", mysql_real_escape_string($id));

		$result = mysql_query($query);

		if ($result) {
			$message = array(
				"message" => "remove_all_buses",
				"_id" => $id
			);
			$response = $this->sendMessage($message);
		}

		return $response;
	}

	public function removeBus($id, $row, $number) {
	$query = sprintf("DELETE FROM buses WHERE school_id='%s' AND number='%s'", mysql_real_escape_string($id), mysql_real_escape_string($number));

		$result = mysql_query($query);

		if ($result) {

			$message = array(
			"message" => "remove_bus",
			"_id" => $id,
			"bus_row" => $row,
				"bus_number" => $number
			);
			$response = $this->sendMessage($message);
		}

		return $response;
	}

	public function getSchoolInfo($id) {
		$query = sprintf("SELECT * FROM config WHERE _id='%s'", mysql_real_escape_string($id));

		$result = mysql_query($query);
		if ($result) {
			while ($row = mysql_fetch_array($result, MYSQL_ASSOC)) {
				return $row;
			}
		}
		return false;
	}

	public function getAllSchools() {
		$query = sprintf("SELECT * FROM config");

		$result = mysql_query($query);

		return $result;
	}
}
?>