package com.oskopek.studyguide.constraint.event;

import com.oskopek.studyguide.constraint.Constraint;
import com.oskopek.studyguide.model.constraints.CourseGroup;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ResourceBundle;

/**
 * The event used for reporting broken course group constraints.
 */
public class BrokenCourseGroupConstraintEvent extends StringMessageEvent {

    private final CourseGroup courseGroup;

    /**
     * Default constructor.
     *
     * @param messages the resource bundle used for resolving messages to their localized versions
     * @param message the message to use as a reason why the constraint is broken
     * @param broken the constraint that was broken and generated this event
     * @param courseGroup the course group that the constraint broke on
     */
    public BrokenCourseGroupConstraintEvent(ResourceBundle messages, String message, Constraint broken,
            CourseGroup courseGroup) {
        super(messages, message, broken);
        this.courseGroup = courseGroup;
    }

    /**
     * Gets the course group that the constraint broke on.
     *
     * @return the course group
     */
    public CourseGroup getCourseGroup() {
        return courseGroup;
    }

    @Override
    public String message() {
        return messages.getString("constraint.courseGroupInvalid") + " " + getMessage();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(getCourseGroup()).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BrokenCourseGroupConstraintEvent)) {
            return false;
        }
        BrokenCourseGroupConstraintEvent that = (BrokenCourseGroupConstraintEvent) o;
        return new EqualsBuilder().appendSuper(super.equals(o)).append(getCourseGroup(), that.getCourseGroup())
                .isEquals();
    }
}
