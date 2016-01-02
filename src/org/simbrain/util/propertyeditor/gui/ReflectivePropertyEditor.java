/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.simbrain.util.propertyeditor.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.simbrain.resource.ResourceManager;
import org.simbrain.util.LabelledItemPanel;
import org.simbrain.util.StandardDialog;
import org.simbrain.util.propertyeditor.ComboBoxWrapper;
import org.simbrain.util.propertyeditor.DisplayOrder;

/**
 * ReflectivePropertyEditor.
 *
 * @author Jeff Yoshimi
 */
public class ReflectivePropertyEditor extends JPanel {

    // TODO: Generalize array stuff
    // - Put arrays in scrollers
    // - Better documenation / readme
    // - Deal with redundant get and is case (then two fields are added)

    /** Getting rid of warning. */
    private static final long serialVersionUID = 1L;

    /**
     * The object whose public fields will be edited using the
     * ReflectivePropertyEditor
     */
    private Object toEdit;

    /** Main item panel. */
    private LabelledItemPanel itemPanel = new LabelledItemPanel();

    /** List of methods that can be edited. Used when committing changes. */
    private List<Method> editableMethods = new ArrayList<Method>();

    /** Map from property names for Color objects to selected selectedColor. */
    private HashMap<String, Color> selectedColor = new HashMap<String, Color>();

    /** List of methods to exclude from the dialog, listed by name. */
    private String[] excludeList = new String[] {};

    /** If true, use superclass methods. */
    private boolean useSuperclass = true;

    /**
     * Associate property names with JComponents that are used to set those
     * values.
     */
    HashMap<String, JComponent> componentMap = new HashMap<String, JComponent>();

    /**
     * Construct a property editor panel only. It's up to the user to embed it
     * in a dialog or other GUI element
     *
     * @param toEdit the object to edit
     */
    public ReflectivePropertyEditor(final Object toEdit) {
        this.toEdit = toEdit;
        this.add(itemPanel);
        initPanel();
    }

    /**
     * Use this constructor to make an editor, adjust its settings, and then
     * initialize it with an object.
     */
    public ReflectivePropertyEditor() {
    }

    /**
     * Returns an action for showing a property dialog for the provided objects.
     *
     * @param object object the object whose properties should be displayed
     * @return the action
     */
    public static AbstractAction getPropertiesDialogAction(final Object object) {
        return new AbstractAction() {

            // Initialize
            {
                putValue(SMALL_ICON, ResourceManager.getImageIcon("Prefs.png"));
                putValue(NAME, "Show properties...");
                putValue(SHORT_DESCRIPTION, "Show properties");
            }

            /**
             * {@inheritDoc}
             */
            public void actionPerformed(ActionEvent arg0) {
                ReflectivePropertyEditor editor = new ReflectivePropertyEditor();
                editor.setUseSuperclass(false);
                editor.setObject(object);
                JDialog dialog = editor.getDialog();
                dialog.setModal(true);
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }

        };
    }

    /**
     * Set object to edit. For use with no argument constructor.
     *
     * @param edit the object to edit
     */
    public void setObject(final Object edit) {
        if (this.toEdit == null) {
            this.toEdit = edit;
            this.add(itemPanel);
            initPanel();
        } // else throw exception?
    }

    /**
     * Returns an dialog containing this property editor.
     *
     * @return parentDialog parent dialog
     */
    public EditorDialog getDialog() {

        final EditorDialog ret = new EditorDialog();
        ret.setContentPane(itemPanel);
        return ret;
    }

    /**
     * Extension of Standard Dialog for Editor Panel
     */
    private class EditorDialog extends StandardDialog {

        @Override
        protected void closeDialogOk() {
            ReflectivePropertyEditor.this.commit();
            dispose();
        }
    }

    /**
     * Set up the main panel.
     *
     * The items in the underlying java class can be ordered using an annotation
     *
     * @DisplayOrder(val = 1) For an example see
     *                   org.simbrain.util.projection.DataColoringManager
     */
    private void initPanel() {

        // Sort methods using display order annotations
        Method[] methods = toEdit.getClass().getMethods();
        Arrays.sort(methods, new Comparator<Method>() {
            @Override
            public int compare(Method method1, Method method2) {
                int int1 = 0;
                int int2 = 0;
                for (Annotation annotation : method1.getDeclaredAnnotations()) {
                    if (annotation instanceof DisplayOrder) {
                        int1 = ((DisplayOrder) annotation).val();
                    }
                }
                for (Annotation annotation : method2.getDeclaredAnnotations()) {
                    if (annotation instanceof DisplayOrder) {
                        int2 = ((DisplayOrder) annotation).val();
                    }
                }
                // Integer.compare(int1, int2) was introduced in java 7
                return Integer.signum(int1 - int2);
            }
        });

        for (final Method method : methods) {
            boolean inSuperClass = (method.getDeclaringClass() != toEdit
                    .getClass());
            // If useSuperClass is false, then if the method is in the base
            // class,
            // this flag should be false.
            boolean skipMethod = !useSuperclass & inSuperClass;

            if (!skipMethod && !inExcludeList(method)) {
                boolean isGetter = (method.getName().startsWith("get") || method
                        .getName().startsWith("is"));
                if (isGetter) {
                    if (hasMatchingSetter(method)) {

                        final String propertyName = getPropertyName(method);
                        final String formattedPropertyName = formatString(propertyName);

                        // Combo Boxes must be wrapped in a ComboBoxable
                        if (method.getReturnType() == ComboBoxWrapper.class) {
                            ComboBoxWrapper boxable = (ComboBoxWrapper) getGetterValue(method);
                            JComboBox comboBox = new JComboBox(
                                    boxable.getObjects());
                            componentMap.put(propertyName, comboBox);
                            comboBox.setSelectedItem(boxable.getCurrentObject());
                            itemPanel.addItem(formattedPropertyName, comboBox);
                        }

                        // Colors
                        else if (method.getReturnType() == Color.class) {
                            final JButton colorButton = new JButton("Set Color");
                            final JPanel colorIndicator = new JPanel();
                            final JPanel colorPanel = new JPanel();
                            colorPanel.add(colorButton);
                            colorIndicator.setSize(20, 20);
                            final Color currentColor = (Color) getGetterValue(method);
                            selectedColor.put(propertyName, currentColor);
                            colorIndicator.setBorder(BorderFactory
                                    .createLineBorder(Color.black));
                            colorIndicator.setBackground(currentColor);
                            colorPanel.add(colorIndicator);
                            colorButton.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent arg0) {
                                    // TODO: Alignment is wrong
                                    Color theColor = JColorChooser.showDialog(
                                            itemPanel, "Choose Color",
                                            selectedColor.get(propertyName));
                                    colorIndicator.setBackground(theColor);
                                    selectedColor.remove(propertyName);
                                    selectedColor.put(propertyName, theColor);
                                }
                            });
                            itemPanel.addItem(formattedPropertyName, colorPanel);
                        } else if (method.getReturnType() == String.class) {
                            JTextField theTextField = getInitializedTextField(method);
                            componentMap.put(propertyName, theTextField);
                            itemPanel.addItem(formattedPropertyName, theTextField);
                        }

                        else if (method.getReturnType() == double[].class) {
                            JTable table = new JTable();
                            table.setGridColor(Color.gray);
                            double[] value = (double[]) getGetterValue(method);
                            DefaultTableModel model = new DefaultTableModel(
                                    value.length, 1);
                            for (int i = 0; i < value.length; i++) {
                                model.setValueAt(value[i], i, 0);
                            }
                            table.setModel(model);
                            table.setBorder(BorderFactory
                                    .createLineBorder(Color.black));
                            componentMap.put(propertyName, table);
                            itemPanel.addItem(formattedPropertyName, table);
                            // JList jList = new JList();
                            // DefaultListModel model = new DefaultListModel();
                            // double[] value = (double[])
                            // getGetterValue(method);
                            // model.setSize(value.length);
                            // for(int i = 0; i < value.length; i++) {
                            // model.set(i, value[i]);
                            // }
                            // jList.setModel(model);
                            // jList.setBorder(BorderFactory.createLineBorder(Color.black));
                            // componentMap.put(propertyName, jList);
                            // itemPanel.addItem(formattedPropertyName, jList);
                        }

                        // Booleans > Checkboxes
                        else if (method.getReturnType() == Boolean.class) {
                            JCheckBox checkBox = new JCheckBox();
                            checkBox.setSelected(Boolean.class.cast(
                                    getGetterValue(method)).booleanValue());
                            componentMap.put(propertyName, checkBox);
                            itemPanel.addItem(formattedPropertyName, checkBox);
                        } else if (method.getReturnType() == boolean.class) {
                            JCheckBox checkBox = new JCheckBox();
                            checkBox.setSelected((Boolean) getGetterValue(method));
                            componentMap.put(propertyName, checkBox);
                            itemPanel.addItem(formattedPropertyName, checkBox);
                        }

                        // Number wrappers (TODO: Find name of this)
                        else if (method.getReturnType() == Integer.class) {
                            JTextField theTextField = getInitializedTextField(method);
                            componentMap.put(propertyName, theTextField);
                            itemPanel.addItem(formattedPropertyName,
                                    theTextField);
                        } else if (method.getReturnType() == Double.class) {
                            JTextField theTextField = getInitializedTextField(method);
                            componentMap.put(propertyName, theTextField);
                            itemPanel.addItem(formattedPropertyName,
                                    theTextField);
                        } else if (method.getReturnType() == Float.class) {
                            JTextField theTextField = getInitializedTextField(method);
                            componentMap.put(propertyName, theTextField);
                            itemPanel.addItem(formattedPropertyName,
                                    theTextField);
                        } else if (method.getReturnType() == Long.class) {
                            JTextField theTextField = getInitializedTextField(method);
                            componentMap.put(propertyName, theTextField);
                            itemPanel.addItem(formattedPropertyName,
                                    theTextField);
                        } else if (method.getReturnType() == Short.class) {
                            JTextField theTextField = getInitializedTextField(method);
                            componentMap.put(propertyName, theTextField);
                            itemPanel.addItem(formattedPropertyName,
                                    theTextField);
                        }

                        // Primitive number types
                        else if (method.getReturnType() == int.class) {
                            JTextField textField = new JTextField();
                            componentMap.put(propertyName, textField);
                            textField.setText((getGetterValue(method))
                                    .toString());
                            itemPanel.addItem(formattedPropertyName, textField);
                        } else if (method.getReturnType() == double.class) {
                            JTextField textField = new JTextField();
                            componentMap.put(propertyName, textField);
                            textField.setText(((Double) getGetterValue(method))
                                    .toString());
                            itemPanel.addItem(formattedPropertyName, textField);
                        } else if (method.getReturnType() == float.class) {
                            JTextField textField = new JTextField();
                            componentMap.put(propertyName, textField);
                            textField.setText((getGetterValue(method))
                                    .toString());
                            itemPanel.addItem(formattedPropertyName, textField);
                        } else if (method.getReturnType() == long.class) {
                            JTextField textField = new JTextField();
                            componentMap.put(propertyName, textField);
                            textField.setText((getGetterValue(method))
                                    .toString());
                            itemPanel.addItem(formattedPropertyName, textField);
                        } else if (method.getReturnType() == short.class) {
                            JTextField textField = new JTextField();
                            componentMap.put(propertyName, textField);
                            textField.setText((getGetterValue(method))
                                    .toString());
                            itemPanel.addItem(formattedPropertyName, textField);
                        }
                    }
                }
            }
        }
    }

    /**
     * Check to see if the method is in the exclude list.
     *
     * @param method the method to check
     * @return true if the method is to be excluded, false otherwse
     */
    private boolean inExcludeList(Method method) {
        for (String name : excludeList) {
            if (getPropertyName(method).equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Commit the changes in the panel to the object being edited.
     */
    public void commit() {
        for (Method m : editableMethods) {
            String propertyName = getPropertyName(m);
            Class<?> argumentType = m.getParameterTypes()[0];

            // Combo Box
            if (argumentType == ComboBoxWrapper.class) {
                final Object selectedObject = ((JComboBox) componentMap
                        .get(propertyName)).getSelectedItem();
                ComboBoxWrapper boxedObject = new ComboBoxWrapper() {
                    public Object getCurrentObject() {
                        return selectedObject;
                    }

                    public Object[] getObjects() {
                        return null;
                    }
                };
                setSetterValue(m, ComboBoxWrapper.class, boxedObject);
            }

            // Double array
            else if (argumentType == double[].class) {
                JTable table = (JTable) componentMap.get(propertyName);
                double[] data = new double[table.getRowCount()];
                for (int i = 0; i < table.getRowCount(); i++) {
                    if (table.getValueAt(i, 0).getClass() == String.class) {
                        data[i] = Double.parseDouble((String) table.getValueAt(
                                i, 0));
                    } else {
                        data[i] = (Double) table.getValueAt(i, 0);
                    }
                }
                setSetterValue(m, argumentType, data);
            }

            // Colors
            else if (argumentType == Color.class) {
                // This one's easy because the colors are already stored in a
                // map
                setSetterValue(m, argumentType, selectedColor.get(propertyName));
            }

            // Booleans
            else if (argumentType == Boolean.class) {
                Boolean newVal = ((JCheckBox) componentMap.get(propertyName))
                        .isSelected();
                setSetterValue(m, argumentType, newVal);
            } else if (argumentType == boolean.class) {
                boolean newVal = ((JCheckBox) componentMap.get(propertyName))
                        .isSelected();
                setSetterValue(m, Boolean.class, new Boolean(newVal));
            }

            // Strings
            else if (argumentType == String.class) {
                String newVal = ((JTextField) componentMap.get(propertyName))
                        .getText();
                setSetterValue(m, argumentType, newVal);
            }

            // Object numbers (TODO)
            else if (argumentType == Integer.class) {
                Integer newVal = Integer.parseInt(((JTextField) componentMap
                        .get(propertyName)).getText());
                setSetterValue(m, argumentType, newVal);
            } else if (argumentType == Double.class) {
                Double newVal = Double.parseDouble(((JTextField) componentMap
                        .get(propertyName)).getText());
                setSetterValue(m, argumentType, newVal);
            } else if (argumentType == Float.class) {
                Float newVal = Float.parseFloat(((JTextField) componentMap
                        .get(propertyName)).getText());
                setSetterValue(m, argumentType, newVal);
            } else if (argumentType == Long.class) {
                Long newVal = Long.parseLong(((JTextField) componentMap
                        .get(propertyName)).getText());
                setSetterValue(m, argumentType, newVal);
            } else if (argumentType == Short.class) {
                Short newVal = Short.parseShort(((JTextField) componentMap
                        .get(propertyName)).getText());
                setSetterValue(m, argumentType, newVal);
            }

            // Primitive Numbers
            else if (argumentType == int.class) {
                Integer newVal = Integer.parseInt(((JTextField) componentMap
                        .get(propertyName)).getText());
                setSetterValue(m, Integer.class, new Integer(newVal));
            } else if (argumentType == double.class) {
                Double newVal = Double.parseDouble(((JTextField) componentMap
                        .get(propertyName)).getText());
                setSetterValue(m, Double.class, new Double(newVal));
            } else if (argumentType == float.class) {
                Float newVal = Float.parseFloat(((JTextField) componentMap
                        .get(propertyName)).getText());
                setSetterValue(m, Float.class, new Float(newVal));
            } else if (argumentType == long.class) {
                Long newVal = Long.parseLong(((JTextField) componentMap
                        .get(propertyName)).getText());
                setSetterValue(m, Long.class, new Long(newVal));
            } else if (argumentType == short.class) {
                Short newVal = Short.parseShort(((JTextField) componentMap
                        .get(propertyName)).getText());
                setSetterValue(m, Short.class, new Short(newVal));
            }
        }

        // System.out.println(toEdit);
    }

    /**
     * Return a property name from a Method object, which is here taken to be
     * the substring after "is", "get", or "set".
     *
     * @param method the method to retrieve the property name from.
     * @return the property name
     */
    private String getPropertyName(Method method) {
        return method.getName().startsWith("is") ? method.getName()
                .substring(2) : method.getName().substring(3);
    }

    /**
     * Returns a text field initialized to the corresponding getter's value.
     *
     * @param m
     * @return
     */
    private JTextField getInitializedTextField(Method m) {
        JTextField textField = new JTextField();
        textField.setText(m.getReturnType().cast(getGetterValue(m)).toString());
        return textField;
    }

    /**
     * Get the return value of getter method.
     *
     * @param theMethod
     * @return
     */
    private Object getGetterValue(final Method theMethod) {
        try {
            return theMethod.invoke(toEdit, (Object[]) null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * True if getter has a matching setter whose one parameter matches the
     * getter's return type.
     */
    private boolean hasMatchingSetter(Method getter) {
        String propertyName = getPropertyName(getter);
        for (Method possibleSetter : toEdit.getClass().getMethods()) {
            if (possibleSetter.getName().equalsIgnoreCase("set" + propertyName)) {
                // Assume just one parameter for a setter
                // System.out.println(possibleSetter.getParameterTypes()[0].getName()
                // + " =? " + getter.getReturnType().getName());
                if (possibleSetter.getParameterTypes()[0].getName() == getter
                        .getReturnType().getName()
                        && (possibleSetter.getParameterTypes().length == 1)) {
                    // At this point set the list of setter methods for use in
                    // commit
                    editableMethods.add(possibleSetter);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Format a string by inserting spaces and capitalizing. This makes the item
     * labels more appealing.
     *
     * Example: convert "heightInPixels" to "Height In Pixels."
     *
     * @param toFormat the String to format, retrieved from a getter or setter.
     * @return the formatted string.
     */
    private String formatString(String toFormat) {
        String ret = "";
        char[] chars = toFormat.toCharArray();
        ret += Character.toString(chars[0]).toUpperCase();
        for (int i = 1; i < chars.length; i++) {
            if (Character.isUpperCase(chars[i])) {
                ret += " " + Character.toString(chars[i]).toUpperCase();
            } else {
                ret += Character.toString(chars[i]);
            }
        }
        return ret;
    }

    /**
     * Set the value of a setter method to a specified value
     *
     * @param theMethod
     * @param theVal
     */
    private void setSetterValue(final Method theMethod, Class<?> type,
            Object theVal) {
        try {
            theMethod.invoke(toEdit, type.cast(theVal));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the useSuperclass
     */
    public boolean isUseSuperclass() {
        return useSuperclass;
    }

    /**
     * @param useSuperclass the useSuperclass to set
     */
    public void setUseSuperclass(boolean useSuperclass) {
        this.useSuperclass = useSuperclass;
    }

    /**
     * Set an exclude list.
     *
     * TODO: Brittle. Only works when the argument-less constructor + setObject
     * is used. Make it so that if the panel is already initialized when this is
     * called, relevant items are removed.
     *
     * @param strings the excludeList to set
     */
    public void setExcludeList(String[] strings) {
        this.excludeList = strings;
    }

    /**
     * Returns the number of items contained in the panel.
     *
     * @return number of items in panel.
     */
    public int getFieldCount() {
        return itemPanel.getMyNextItemRow();
    }

}
