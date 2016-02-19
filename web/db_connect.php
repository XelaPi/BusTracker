<?php

class DB_CONNECT {
 
    function __construct() {
        $this->connect();
    }
 
    // destructor
    function __destruct() {
        $this->close();
    }
 
    /**
     * Function to connect with database
     */
    function connect() {
        require_once __DIR__ . '/db_config.php';
		
        $con = mysql_connect(DB_SERVER, DB_USER, DB_PASSWORD) or die(mysql_error());
		
        $db = mysql_select_db(DB_DATABASE) or die(mysql_error()) or die(mysql_error());
		
        return $con;
    }
 
    /**
     * Function to close db connection
     */
    function close() {
        mysql_close();
    }
 
}
 
?>