public void doDownload() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Remote Name");
    dialog.setHeaderText("Enter the name of\nthe remote file to download.");
    dialog.setX(75.0D);
    dialog.showAndWait();
    String remoteName = dialog.getEditor().getText();
    FileChooser chooser = new FileChooser();
    chooser.setInitialDirectory(new File(this.tfFolder.getText()));
    chooser.setTitle("Select/Enter the Name of the File for Saving the Download");
    File localFile = chooser.showSaveDialog((Window)this.stage);
    if (localFile == null) {
      log("Canceled!");
      return;
    } 
    Thread dlThread = new DownLoadThread(remoteName, localFile);
    dlThread.start();
  }
  
  