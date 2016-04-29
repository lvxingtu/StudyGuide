package com.oskopek.studyguide.controller;

import com.oskopek.studyguide.model.CourseEnrollment;
import com.oskopek.studyguide.model.Semester;
import com.oskopek.studyguide.view.AbstractFXMLPane;
import com.oskopek.studyguide.view.SemesterBoxPane;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.DragEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for {@link SemesterBoxPane}.
 * Represents a single {@link Semester} in a {@link com.oskopek.studyguide.model.SemesterPlan}
 *
 * @see com.oskopek.studyguide.view.SemesterPane
 */
public class SemesterBoxController extends AbstractController<SemesterBoxPane> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static int index = 0;

    @FXML
    private TextField semesterNameArea;

    @FXML
    private TableView<CourseEnrollment> semesterTable;

    @FXML
    private TableColumn<CourseEnrollment, String> idColumn;

    @FXML
    private TableColumn<CourseEnrollment, String> nameColumn;

    @FXML
    private TableColumn<CourseEnrollment, Number> creditsColumn;

    @FXML
    private TableColumn<CourseEnrollment, Boolean> fulfilledColumn;

    @FXML
    private TableColumn<CourseEnrollment, String> removeColumn;


    private Semester semester;

    /**
     * Initializes the listener for Semester name changes.
     *
     * @see #onSemesterNameChange()
     */
    @FXML
    private void initialize() {
        semesterNameArea.textProperty().addListener((observable) -> onSemesterNameChange());

        idColumn.setCellValueFactory(cellData -> cellData.getValue().getCourse().idProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().getCourse().nameProperty());
        creditsColumn.setCellValueFactory(
                cellData -> cellData.getValue().getCourse().getCredits().creditValueProperty());
        fulfilledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(fulfilledColumn)); // TODO make editable
        fulfilledColumn.setCellValueFactory(cellData -> cellData.getValue().fulfilledProperty());
        removeColumn.setCellFactory((final TableColumn<CourseEnrollment, String> param) ->
                new TableCell<CourseEnrollment, String>() {
                    final Button removeButton = new Button("✗");

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(null);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            removeButton.setOnAction((ActionEvent event) -> {
                                CourseEnrollment enrollment = getTableView().getItems().get(getIndex());
                                semester.removeCourseEnrollment(enrollment);
                                logger.debug("Removing Course Enrollment ({}) from Semester ({}).",
                                        enrollment, semester);
                            });
                            setGraphic(removeButton);
                        }
                    }
                });

        EventHandler<Event> d = event -> { // TODO change to on focus
            CourseEnrollment e = semesterTable.getSelectionModel().getSelectedItem();
            logger.debug("Focused on CourseEnrollment {}", e);
            // TODO view course details
        };
        semesterTable.setOnMouseClicked(d);
        semesterTable.setOnKeyReleased(d);
    }

    /**
     * Initialize a new {@link Semester} instance into this box.
     * Needed, because JavaFX's initialize method runs too soon (before we have a reference to the main app).
     *
     * @see AbstractController#setStudyGuideApplication(com.oskopek.studyguide.view.StudyGuideApplication)
     * @see SemesterController#addSemester(Semester)
     */
    public void initializeEmptySemester() {
        semester = new Semester("Semester" + index++);
        setSemester(semester);
        studyGuideApplication.getStudyPlan().getSemesterPlan().addSemester(semester);
    }


    /**
     * Set the semester into this box. Does not update the model.
     *
     * @param semester non-null
     */
    public void setSemester(Semester semester) {
        if (semester == null) {
            throw new IllegalArgumentException("Semester cannot be null.");
        }
        this.semester = semester;
        semesterNameArea.setText(semester.getName());
        semesterTable.itemsProperty().bindBidirectional(semester.courseEnrollmentListProperty());
    }

    /**
     * Get the parent controller. Used for inter-semester data exchange.
     *
     * @return non-null controller for {@link com.oskopek.studyguide.view.SemesterPane}
     * @see #onRemoveSemester()
     * @see #onDragDetected()
     * @see #onDragDropped(DragEvent)
     */
    private SemesterController getParentController() {
        return (SemesterController) viewElement.getParent().getController();
    }

    /**
     * Handles removing this whole box. Calls {@link SemesterController#removeSemester(SemesterBoxPane)}.
     */
    @FXML
    public void onRemoveSemester() {
        semesterTable.itemsProperty().unbindBidirectional(semester.courseEnrollmentListProperty());
        studyGuideApplication.getStudyPlan().getSemesterPlan().removeSemester(semester);
        getParentController().removeSemester(this.viewElement);
    }

    /**
     * Handles changing the name of this semester box. The name has to be unique (different than others in the list).
     */
    @FXML
    public void onSemesterNameChange() {
        String newName = semesterNameArea.getText();
        if (semester.getName().equals(newName)) {
            return;
        }
        if (studyGuideApplication.getStudyPlan().getSemesterPlan().getSemesterList().contains(new Semester(newName))) {
            AbstractFXMLPane.showAlert(Alert.AlertType.WARNING,
                    messages.getString("semesterBox.nameNotUnique"));
            semesterNameArea.setText(semester.getName());
        } else {
            semester.setName(newName);
        }
    }

    /**
     * Handles the start of a {@link com.oskopek.studyguide.model.courses.Course} drag and drop event
     * at the data source. Calls {@link SemesterController#dragDetected(SemesterBoxPane)}.
     */
    @FXML
    public void onDragDetected() {
        getParentController().dragDetected(this.viewElement);
        logger.debug("Drag detected {}", this.semester);
    }

    /**
     * Handles the end of a {@link com.oskopek.studyguide.model.courses.Course} drag and drop event at the data target.
     * Calls {@link SemesterController#dragDropped(SemesterBoxPane)}.
     *
     * @param e the drag event
     */
    @FXML
    public void onDragDropped(DragEvent e) {
        getParentController().dragDropped(this.viewElement);
        logger.debug("Drag dropped {}", this.semester);
    }

    /**
     * Handles the end of a {@link com.oskopek.studyguide.model.courses.Course} drag and drop event at the data source.
     * Calls {@link SemesterController#dragDone(SemesterBoxPane)}.
     *
     * @param e the drag event
     */
    @FXML
    public void onDragDone(DragEvent e) {
        logger.debug("Debug done {}, source {}", this.semester,
                ((SemesterBoxController) e.getGestureSource()).semester);
        getParentController().dragDone(this.viewElement);
        logger.debug("Drag done {}", this.semester);
    }

}
