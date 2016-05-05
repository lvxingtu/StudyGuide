package com.oskopek.studyguide.constraint;

import com.oskopek.studyguide.model.CourseEnrollment;
import com.oskopek.studyguide.model.constraints.CourseGroup;
import com.oskopek.studyguide.model.courses.Course;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Constraint on the level of individual {@link com.oskopek.studyguide.model.constraints.CourseGroup}s.
 */
public abstract class CourseGroupConstraint extends DefaultConstraint {

    private CourseGroup courseGroup;

    @Inject
    private Event<BrokenCourseGroupConstraintEvent> brokenEvent;

    /**
     * Private default constructor, needed by CDI.
     */
    protected CourseGroupConstraint() {
        // needed by CDI
    }

    /**
     * Default constructor.
     *
     * @param courseGroup the referenced course group
     */
    public CourseGroupConstraint(CourseGroup courseGroup) {
        this.courseGroup = courseGroup;
    }

    /**
     * Get the checked course group.
     *
     * @return the course group
     */
    public CourseGroup getCourseGroup() {
        return courseGroup;
    }

    /**
     * The method should verify if the given constraint was broken, and if so,
     * call the {@link #fireBrokenEvent(String)} with the specific reason.
     */
    protected abstract void validate();

    @Override
    public void validate(@Observes Course changed) {
        if (courseGroup.courseListProperty().contains(changed)) {
            validate();
        }
    }

    @Override
    public void validate(@Observes CourseEnrollment changed) {
        validate(changed.getCourse());
    }

    /**
     * Used for firing the {@link BrokenCourseGroupConstraintEvent} if the constraint is broken.
     *
     * @param message the reason why the constraint is broken
     */
    public void fireBrokenEvent(String message) {
        brokenEvent.fire(new BrokenCourseGroupConstraintEvent(message, courseGroup));
    }

    @Override
    public void fireBrokenEvent(String message, Course changed) {
        fireBrokenEvent(message);
    }

    @Override
    public void fireBrokenEvent(String message, CourseEnrollment changed) {
        fireBrokenEvent(message);
    }
}