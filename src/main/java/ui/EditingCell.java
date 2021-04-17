package ui;

import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

public class EditingCell<H, I> extends TableCell<H, I> {

	protected TextField textField;
	private final Class<I> persistentClass;

	public EditingCell() {
		persistentClass = (Class<I>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
	}

	@Override
	public void startEdit() {
		if (!isEmpty()) {
			super.startEdit();
			createTextField();
			setText(null);
			setGraphic(textField);
			textField.requestFocus();
			textField.selectAll();
		}
	}

	@Override
	public void commitEdit(I newValue) {
		super.commitEdit(newValue);
		commitEditHandler(newValue);

		updateItem(newValue, false);
	}

	public void commitEditHandler(I newValue){}

	@Override
	public void cancelEdit() {
		super.cancelEdit();

		setText(getItem().toString());
		setGraphic(null);
	}

	@Override
	public void updateItem(I item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			updateItemHandler(item);
			if (isEditing()) {
				if (textField != null) {
					textField.setText(getString());
				}
				setText(null);
				setGraphic(textField);
			} else {
				setText(getString());
				setGraphic(null);
			}
		}
	}

	public void updateItemHandler(I item){}

	private void createTextField() {
		textField = new TextField(getString());
		textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
		textField.focusedProperty().addListener((observableValue, oldValue, newValue) -> {
			if (!newValue) {
				commitEdit(convert(textField.getText(), persistentClass));
			}
		});
		textField.setOnKeyPressed(t -> {
			if (t.getCode() == KeyCode.ENTER) {
				commitEdit(convert(textField.getText(), persistentClass));
			} else if ((t.getCode() == KeyCode.TAB && !t.isShiftDown()) || t.getCode() == KeyCode.DOWN) {
				EditingCell.this.getTableView().requestFocus();//why does it lose focus??
				EditingCell.this.getTableView().getSelectionModel().selectBelowCell();
			} else if (t.getCode() == KeyCode.TAB || t.getCode() == KeyCode.UP) {
				EditingCell.this.getTableView().requestFocus();//why does it lose focus??
				EditingCell.this.getTableView().getSelectionModel().selectAboveCell();
			} else if (t.getCode() == KeyCode.ESCAPE) {
				cancelEdit();
			}
		});
		/*textField.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent t) {
				if (t.getCode().isDigitKey()) {
					if (CellField.isLessOrEqualOneSym()) {
						CellField.addSymbol(t.getText());
					} else {
						CellField.setText(textField.getText());
					}
					textField.setText(CellField.getText());
					textField.deselect();
					textField.end();
					textField.positionCaret(textField.getLength() + 2);//works sometimes

				}
			}
		});*/
	}

	protected String getString() {
		return getItem() == null ? "" : getItem().toString();
	}

	public static <I> I convert(String from, Class<I> to) {
		if (from == null) return null;

		//convert directly if possible
		if (to.isAssignableFrom(from.getClass())) return to.cast(from);

		try {
			Method m = to.getMethod("valueOf", String.class);
			Object o = m.invoke(to, from);
			return to.cast(o);
		} catch (Exception e) {
			throw new RuntimeException("Cannot convert from "
					+ from.getClass().getName() + " to " + to.getName()
					+ ". Conversion failed with " + e.getMessage(), e);
		}
	}

}
