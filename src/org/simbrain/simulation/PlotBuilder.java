package org.simbrain.simulation;

import org.simbrain.plot.timeseries.TimeSeriesModel;
import org.simbrain.plot.timeseries.TimeSeriesPlotComponent;

//TODO: Possibly make specific to time series plots... not sure if there is a way to consolidate the plots in one "builder"
public class PlotBuilder {

    private final TimeSeriesPlotComponent timeSeriesComponent;

    private final TimeSeriesModel model;

    public PlotBuilder(TimeSeriesPlotComponent tsc) {
        this.timeSeriesComponent = tsc;
        model = tsc.getModel();
    }

    /**
     * @return the timeSeriesComponent
     */
    public TimeSeriesPlotComponent getTimeSeriesComponent() {
        return timeSeriesComponent;
    }

    /**
     * @return the model
     */
    public TimeSeriesModel getTimeSeriesModel() {
        return model;
    }

}
