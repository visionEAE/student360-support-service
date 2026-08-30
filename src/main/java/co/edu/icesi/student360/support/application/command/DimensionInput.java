package co.edu.icesi.student360.support.application.command;

import co.edu.icesi.student360.support.domain.model.Mood;
import co.edu.icesi.student360.support.domain.model.WellbeingDimension;
import java.util.List;

public record DimensionInput(
    WellbeingDimension dimension, Mood mood, List<String> needs, String note) {}
