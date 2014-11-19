<?php

  // Database Configuration
  define("Database_NAME", "davisengeler_daycare");
  define("Database_HOST", "localhost");
  define("Database_USER", "daycare");
  define("Database_PASS", "engelerhetzel");

  function connectDB()
  {
    $connection = mysqli_connect(Database_HOST, Database_USER, Database_PASS, Database_NAME) or die("Could not connect to database");
    return $connection;
  }  

  // Provides a check to make sure the device making calls to the API is a verified device
  function deviceIsVerified($database, $deviceID)
  {
    if ($result = mysqli_query($database, "CALL device_verified($deviceID)"))
    {
      $row = mysqli_fetch_array($result);
      if ($row["Verified"] == 1)
      {
        // the device is verified
        return true;
      }
      else
      {
        // the device was denied or still pending
        return false;
      }
    }
    else
    {
      // Had an issue with the database
      return false;
    }
  }

?>
