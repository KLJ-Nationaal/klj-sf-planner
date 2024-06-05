/**
 * Copyright (c) 2011-2020, JFXtras
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *    Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    Neither the name of the organization nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ui.visualization.jfxtras.internal.scene.control.skin.agenda.base24hour;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableObjectProperty;
import javafx.css.Styleable;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.util.Pair;
import jfxtras.css.CssMetaDataForSkinProperty;
import jfxtras.css.converters.DoubleConverter;
import persistence.Marshalling;
import ui.visualization.jfxtras.internal.scene.control.skin.agenda.AgendaSkin;
import ui.visualization.jfxtras.scene.control.agenda.Agenda;
import ui.visualization.jfxtras.scene.control.agenda.InschrijvingInterface;

import java.util.*;

/**
 * @author Tom Eugelink
 */
// TODO: number of call to determineDisplayedColumns, can we cache?
abstract public class AgendaSkinTimeScale24HourAbstract<H, I> extends SkinBase<Agenda<H, I>> implements AgendaSkin<H>
{
	// ==================================================================================================================
	// CONSTRUCTOR

	public AgendaSkinTimeScale24HourAbstract(Agenda<H, I> control)	{
		super(control);
		this.control = control;
		construct();
	}
	protected final Agenda<H, I> control;

	/**
	 * Reconstruct the UI part
	 */
	protected void reconstruct() {
		weekBodyPane.reconstruct();
		weekHeaderPane.reconstruct();
		
		// initial setup
		refresh();
	}
	
	/*
	 * construct the component
	 */
	private void construct()
	{	
		// setup component
		createNodes();
		
		// react to changes in the appointments 
		getSkinnable().appointments().addListener(appointmentsListChangeListener);

        // clean up removed appointments from appointmentNodeMap
        getSkinnable().appointments().addListener(appointmentNodeMapCleanUpListChangeListener);

		// initial setup
		refresh();
	}
	private final ListChangeListener<I> appointmentsListChangeListener = (changes) -> setupAppointments();
    private final ListChangeListener<I> appointmentNodeMapCleanUpListChangeListener = (changes) -> {
        while (changes.next()) {
            if (changes.wasRemoved()) {
                changes.getRemoved().stream().forEach(a -> appointmentNodeMap().remove(System.identityHashCode(a)));
            }
        }
    };

	public void dispose() {
		// remove listeners
		getSkinnable().appointments().removeListener((InvalidationListener) appointmentsListChangeListener);
        getSkinnable().appointments().removeListener((InvalidationListener) appointmentNodeMapCleanUpListChangeListener);
		
		// reset style classes
		getSkinnable().getStyleClass().clear();
		getSkinnable().getStyleClass().add(Agenda.class.getSimpleName());

		// continue
		super.dispose();
	}

	/**
	 * Have all days reconstruct the appointments
	 */
	public void setupAppointments() {
		for (DayHeaderPane<H> lDay : weekHeaderPane.dayHeaderPanes) {
			lDay.setupAppointments();
		}
		for (DayBodyPane<H, I> lDay : weekBodyPane.dayBodyPanes) {
			lDay.setupAppointments();
		}
		calculateSizes(); // must be done after setting up the panes
	}

	public void refresh() {
		setupAppointments();
	}

	@Override
    public Pane getNodeForPopup(InschrijvingInterface appointment) { return appointmentNodeMap.get(System.identityHashCode(appointment)); }
    final private Map<Integer, Pane> appointmentNodeMap = new HashMap<>();
    Map<Integer, Pane> appointmentNodeMap() { return appointmentNodeMap; }
	
	// ==================================================================================================================
	// StyleableProperties
	
    /**
     * snapToMinutes
     * I am clueless why the Integer version of this property gets a double pushed in (which results in a ClassCastException)
     */
	// TBEERNOT: reattempt converting this to Integer
    public final ObjectProperty<Double> snapToMinutesProperty() { return snapToMinutesProperty; }
    private final ObjectProperty<Double> snapToMinutesProperty = new SimpleStyleableObjectProperty<>(StyleableProperties.SNAPTOMINUTES_CSSMETADATA, StyleableProperties.SNAPTOMINUTES_CSSMETADATA.getInitialValue(null));
    public final void setSnapToMinutes(double value) { snapToMinutesProperty().set(value); }
    public final double getSnapToMinutes() { return snapToMinutesProperty.get().intValue(); }
    public final AgendaSkinTimeScale24HourAbstract<H, I> withSnapToMinutes(double value) { setSnapToMinutes(value); return this; }

    // -------------------------
        
    private static class StyleableProperties 
    {
        private static final CssMetaData<Agenda<?, ?>, Double> SNAPTOMINUTES_CSSMETADATA = new CssMetaDataForSkinProperty<Agenda<?, ?>, AgendaSkinTimeScale24HourAbstract<?, ?>, Double>("-fxx-snap-to-minutes", DoubleConverter.getInstance(), (double)Marshalling.MINMINUTEN) {
        	@Override 
        	protected ObjectProperty<Double> getProperty(AgendaSkinTimeScale24HourAbstract<?, ?> s) {
            	return s.snapToMinutesProperty;
            }
        };
        
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static  {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(SkinBase.getClassCssMetaData());
            styleables.add(SNAPTOMINUTES_CSSMETADATA);
            STYLEABLES = Collections.unmodifiableList(styleables);                
        }
    }
    
    /** 
     * @return The CssMetaData associated with this class, which may include the
     * CssMetaData of its super classes.
     */    
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /**
     * This method should delegate to {@link Node#getClassCssMetaData()} so that
     * a Node's CssMetaData can be accessed without the need for reflection.
     * @return The CssMetaData associated with this node, which may include the
     * CssMetaData of its super classes.
     */
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }
        


	// ==================================================================================================================
	// DRAW
	
	/**
	 * construct the nodes
	 */
	private void createNodes()
	{
		// when switching skin, remove any old stuff
		getChildren().clear();
		if (borderPane != null) {
			layoutHelp.dragPane.getChildren().remove(borderPane);
		}
		
		// we use a borderpane
		borderPane = new BorderPane();
		borderPane.prefWidthProperty().bind(getSkinnable().widthProperty()); // the border pane is the same size as the whole skin
		borderPane.prefHeightProperty().bind(getSkinnable().heightProperty());
		getChildren().add(borderPane);
		
		// borderpane center
		weekBodyPane = new WeekBodyPane();
		weekScrollPane = new ScrollPane();
		weekScrollPane.setContent(weekBodyPane);
		weekScrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		weekScrollPane.setFitToWidth(true);
		weekScrollPane.setPannable(false); // panning would conflict with creating a new appointment
		weekScrollPane.getContent().setOnScroll(scrollEvent -> {
			double deltaY = scrollEvent.getDeltaY() * 0.002;
			weekScrollPane.setVvalue(weekScrollPane.getVvalue() - deltaY);
		});
		borderPane.setCenter(weekScrollPane);
		// bind to the scrollpane's viewport
		weekScrollPane.viewportBoundsProperty().addListener( (observable) -> {
			calculateSizes();
		});
		
		// borderpane top: header has to be created after the content, because there is a binding
		weekHeaderPane = new WeekHeaderPane(); // must be done after the WeekBodyPane
		weekHeaderPane.setTranslateX(1); // correct for the scrollpane
		borderPane.setTop(weekHeaderPane);
		
		// the borderpane is placed in the drag pane, so DragPane can catch mouse events
		getChildren().remove(borderPane);
		layoutHelp.dragPane.getChildren().add(borderPane);
		getChildren().add(layoutHelp.dragPane);
		
		// style
		getSkinnable().getStyleClass().add(getClass().getSimpleName()); // always add self as style class, because CSS should relate to the skin not the control		
	}
	protected BorderPane borderPane = null;
	private WeekHeaderPane weekHeaderPane = null;
	private ScrollPane weekScrollPane = null;
	private WeekBodyPane weekBodyPane = null;

	// ==================================================================================================================
	// PANES

	/**
	 * Responsible for rendering the day headers within the week
	 */
	class WeekHeaderPane extends Pane {
		final List<DayHeaderPane<H>> dayHeaderPanes = new ArrayList<>();

		public WeekHeaderPane() {
			construct();
		}
		
		private void construct() {
			// one day header pane per day body pane 
			for (DayBodyPane<H, I> dayBodyPane : weekBodyPane.dayBodyPanes) {
				// create pane
				DayHeaderPane<H> lDayHeader = new DayHeaderPane<>(dayBodyPane.columnValueObjectProperty.get(), layoutHelp); // associate with a day, so we can use its administration. This needs only be done once
				
				// layout in relation to day panes
				lDayHeader.layoutXProperty().bind(dayBodyPane.layoutXProperty()); // same x position as the body			
				lDayHeader.layoutYProperty().set(0);
				lDayHeader.prefWidthProperty().bind(dayBodyPane.prefWidthProperty()); // same width as the body			
				lDayHeader.prefHeightProperty().bind(heightProperty()); // same height as the week pane
				getChildren().add(lDayHeader);

				String lWeekendOrWeekday = (dayHeaderPanes.size() % 2 == 0 ? "weekend" : "weekday");
				lDayHeader.getStyleClass().removeAll("weekend", "weekday");
				lDayHeader.getStyleClass().add(lWeekendOrWeekday);

				// remember
				dayHeaderPanes.add(lDayHeader);
			}
			
			prefWidthProperty().bind(weekBodyPane.widthProperty()); // same width as the weekpane
			prefHeightProperty().bind(layoutHelp.headerHeightProperty);
		}
		
		private void reconstruct() {
			dayHeaderPanes.clear();
			getChildren().clear();
			construct();
		}
	}

	/**
	 * Responsible for rendering the days within the week
	 */
	class WeekBodyPane extends Pane {
		final List<DayBodyPane<H, I>> dayBodyPanes = new ArrayList<>();

		public WeekBodyPane() {
			getStyleClass().add("Week");
			construct();
		}
		
		private void construct() {
			getChildren().add(new TimeScale24Hour(this, layoutHelp));
			
			int i = 0;
			ObservableList<H> cols = layoutHelp.skinnable.columns();
			for (H columnValue : cols)
			{
				DayBodyPane<H, I> lDayPane = new DayBodyPane<>(columnValue, layoutHelp);
				lDayPane.layoutXProperty().bind(layoutHelp.dayWidthProperty.multiply(i).add(layoutHelp.dayFirstColumnXProperty));
				lDayPane.layoutYProperty().set(0.0);
				lDayPane.prefWidthProperty().bind(layoutHelp.dayWidthProperty);
				lDayPane.prefHeightProperty().bind(layoutHelp.dayHeightProperty.add(layoutHelp.headerHeightProperty));
				getChildren().add(lDayPane);

				String lWeekendOrWeekday = (i % 2 == 0 ? "weekend" : "weekday" );
				lDayPane.getStyleClass().removeAll("weekend", "weekday");
				lDayPane.getStyleClass().add(lWeekendOrWeekday);

				// remember
				dayBodyPanes.add(lDayPane);
				i++;
			}
		}
		
		void reconstruct() {
			dayBodyPanes.clear();
			getChildren().clear();
			construct();
		}
	}
	
	// ==================================================================================================================
	// SUPPORT

	/**
	 * These values can not be determined by binding them to other values, because their calculation is too complex
	 */
	private void calculateSizes()
	{
		// header
		int lMaxOfWholeDayAppointments = 0;
		for (DayHeaderPane<H> lDayHeaderPane : weekHeaderPane.dayHeaderPanes)
		{
			int lNumberOfWholeDayAppointments = lDayHeaderPane.getNumberOfWholeDayAppointments();
			lMaxOfWholeDayAppointments = Math.max(lMaxOfWholeDayAppointments, lNumberOfWholeDayAppointments);
		}
		layoutHelp.highestNumberOfWholedayAppointmentsProperty.set(lMaxOfWholeDayAppointments);

		// day columns
		if (weekScrollPane.viewportBoundsProperty().get() != null) {
			layoutHelp.dayWidthProperty.set( (weekScrollPane.viewportBoundsProperty().get().getWidth() - layoutHelp.timeWidthProperty.get()) / weekHeaderPane.dayHeaderPanes.size() );
		}
		
		// hour height
		double lScrollbarSize = new ScrollBar().getWidth();
		layoutHelp.hourHeightProperty.set( layoutHelp.textHeightProperty.get() * 15 + 10 ); // 10 is padding
		if (weekScrollPane.viewportBoundsProperty().get() != null && (weekScrollPane.viewportBoundsProperty().get().getHeight() - lScrollbarSize) > layoutHelp.hourHeightProperty.get() * 24) {
			// if there is more room than absolutely required, let the height grow with the available room
			layoutHelp.hourHeightProperty.set( (weekScrollPane.viewportBoundsProperty().get().getHeight() - lScrollbarSize) / 24 );
		}
	}
	private final LayoutHelp<H, I> layoutHelp = new LayoutHelp<>(getSkinnable(), this);
	
	public Pair<H, Integer> convertClickInSceneToDateTime(double x, double y) {
		// the click has only value in either the day panes 
		for (DayBodyPane<H, I> lDayPane : weekBodyPane.dayBodyPanes) {
			int  lTime = lDayPane.convertClickInSceneToDateTime(x, y);
			if (lTime == -1) return new Pair<>(lDayPane.columnValueObjectProperty.getValue(), lTime);
		}
		// or the day header panes
		for (DayHeaderPane<H> lDayHeaderPane : weekHeaderPane.dayHeaderPanes) {
			int lTime = lDayHeaderPane.convertClickInSceneToDateTime(x, y);
			if (lTime != 0) return new Pair<>(lDayHeaderPane.columnValueObjectProperty.getValue(), lTime);
		}
		return null;
	}
	
	
	// ==================================================================================================================
	// Print

    /**
     * Prints the current skin using the given printer job.
     * <p>This method does not modify the state of the job, nor does it call
     * {@link PrinterJob#endJob}, so the job may be safely reused afterwards.
     * 
     * @param job printer job used for printing
     * @since JavaFX 8.0
     */
    public void print(PrinterJob job) {
        float width = 5000; 
        float height = 5000; 
        
		// we use a borderpane
		BorderPane borderPane = new BorderPane();
		borderPane.prefWidthProperty().set(width);
		borderPane.prefHeightProperty().set(height);
		
		// borderpane center
		WeekBodyPane weekBodyPane = new WeekBodyPane();
		borderPane.setCenter(weekBodyPane);
		
		// borderpane top: header has to be created after the content, because there is a binding
		WeekHeaderPane weekHeaderPane = new WeekHeaderPane(); // must be done after the WeekBodyPane
		borderPane.setTop(weekHeaderPane);
		
		// style
		borderPane.getStyleClass().add(Agenda.class.getSimpleName()); // always add self as style class, because CSS should relate to the skin not the control		
		borderPane.getStyleClass().add(getClass().getSimpleName()); // always add self as style class, because CSS should relate to the skin not the control		

		// scale to match page
        PageLayout pageLayout = job.getJobSettings().getPageLayout();
        double scaleX = pageLayout.getPrintableWidth() / borderPane.getBoundsInParent().getWidth();
		double scaleY = pageLayout.getPrintableHeight() / borderPane.getBoundsInParent().getHeight();
		scaleY *= 0.9; // for some reason the height doesn't fit
		borderPane.getTransforms().add(new Scale(scaleX, scaleY));

        // print
        job.printPage(pageLayout, borderPane);
    }
}
