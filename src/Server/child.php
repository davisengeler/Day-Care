<?php
  include("config.php");

  // Connect to database
  $database = mysqli_connect(Database_HOST, Database_USER, Database_PASS, Database_NAME) or die("Could not connect to database");

  class Child
  {
    public $childID, $ssn, $firstName, $lastName, $dob, $parentID, $classID;
  }

  // Adds a child
  function addChild($database, $ssn, $firstName, $lastName, $dob, $parentID, $classID)
  {
    if (mysqli_query($database, "CALL add_new_child($ssn, '$firstName', '$lastName', $dob, $parentID, $classID);"))
    {
      // New Request Submitted
      return generateResult(true, "The child has been added to the parent account.");
    }
    else
    {
      // New Request Denied
      return generateResult(false, "Something was wrong with this request to add a child. " . mysqli_error($database));
    }
  }

  // Adds a note for an array of children
  function addNote($database, $message, $noteType, $subjectID, $children)
  {
    // Sets up a status message string
    $statuses = "";
    $allSuccessful = true;

    // Decodes array of children
    $childrenArray = json_decode($children, true);

    // Prepares the note
    if($result = mysqli_query($database, "CALL prepare_note('$message', $subjectID, $noteType);"))
    {
      $note = mysqli_fetch_array($result);
      $noteID = $note["LAST_INSERT_ID()"];
      foreach ($childrenArray as $currentChild)
      {
        mysqli_next_result($database);
        if($result = mysqli_query($database, "CALL link_note($noteID, $currentChild);"))
        {
          $statuses = $statuses . "Note added to ChildID " . $currentChild . " successfully. ";
        }
        else
        {
          $statuses = $statuses . "Note failed to be added to ChildID " . $currentChild . ". ";
          $allSuccessful = false;
        }
      }
    }
    return generateResult($allSuccessful, $statuses);
  }

  // Gets info on a child
  function getChild($database, $type, $parameter)
  {
    switch ($type)
    {
      case "childids":
        $databaseCall = "CALL get_child_info";
        break;
      case "ssn":
        $databaseCall = "CALL get_child_info_by_ssn";
        break;
    }

    $infoArray = json_decode($parameter, true);
    $childArray = array();

    foreach ($infoArray as $currentInfo)
    {
      mysqli_next_result($database);
      if ($result = mysqli_query($database, $databaseCall . "(" . $currentInfo . ");"))
      {
        $child = new Child;
        $row = mysqli_fetch_array($result);
        $child->childID = $row["ChildID"];
        $child->ssn = $row["SSN"];
        $child->firstName = $row["FirstName"];
        $child->lastName = $row["LastName"];
        $child->dob = $row["DOB"];
        $child->parentID = $row["ParentID"];
        $child->classID = $row["ClassID"];

        if ($child->ssn != null)
        {
          $childArray[] = $child;
        }
      }
      else
      {

      }
    }

    return $childArray;
  }





  // =============================================




  // What is the request for?
  // Add New Child
  if (isset($_GET['add']))
  {
    $apiResponse = addChild(
      $database,
      $_GET["ssn"],
      $_GET["firstname"],
      $_GET["lastname"],
      $_GET["dob"],
      $_GET["parentid"],
      $_GET["classid"]);
    echo json_encode($apiResponse);
  }
  // Gather the info about a child
  else if (isset($_GET['getinfo']))
  {
    if ($_GET['ssn'])
    {
      $type = "ssn";
    }
    elseif ($_GET['childids'])
    {
      $type = "childids";
    }

    $apiResponse = getChild($database, $type, $_GET[$type]);
    echo json_encode($apiResponse);
  }
  else if (isset($_GET['addnote']))
  {
    $apiResponse = addNote($database, $_GET['message'], $_GET['notetype'], $_GET['subjectid'], $_GET['children']);
    echo json_encode($apiResponse);
  }
  else if (isset($_GET['setclass']))
  {
    $childID = $_GET['childid'];
    $teacherID = $_GET['teacherid'];

    if ($result = mysqli_query($database, "CALL change_child_class($childID, $teacherID);"))
    {
      echo json_encode(generateResult(true, "The child's classroom was changed successfully."));
    }
    else
    {
      echo json_encode(generateResult(false, "There was an error changing that child's classroom: " . mysqli_error($database)));
    }
  }
  // Get Child Notes
  // TODO: Something weird with the loop when it gets multiple child IDs.
  else if (isset($_GET['getnotes']))
  {
    $childIDs = json_decode($_GET['childids']);
    $noteList = array();

    //TODO: Echoing the errors as the loops goes could give an overall invalid JSON string, therefore not being decodable.
    foreach ($childIDs as $currentChild)
    {
      if ($result = mysqli_query($database, "CALL get_notes($currentChild);"))
      {
        // Gathers all the notes for the child
        while($row = mysqli_fetch_array($result))
        {
          $note = array();
          $note["ChildID"] = $currentChild;
          $note["NoteID"] = $row["NoteID"];
          $note["Message"] = $row["Message"];
          $note["SubjectID"] = $row["SubjectID"];
          $note["NoteType"] = $row["NoteType"];

          if ($note["NoteID"] != null)
          {
            $noteList[] = $note;
          }
        }
      }
      mysqli_next_result($database);
    }
    echo json_encode($noteList);
  }

  function generateResult($successful, $message)
  {
    $response = array(
      "successful" => $successful,
      "statusMessage" => $message
      );

    return $response;
  }
?>
