/*
 * GUIUtilities.java - Various GUI utility functions
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1999, 2004 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
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

package org.gjt.sp.jedit;

//{{{ Imports

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.Dialog;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import java.net.URL;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.*;

import org.gjt.sp.jedit.browser.VFSFileChooserDialog;

import org.gjt.sp.jedit.gui.EnhancedButton;
import org.gjt.sp.jedit.gui.FloatingWindowContainer;
import org.gjt.sp.jedit.gui.SplashScreen;
import org.gjt.sp.jedit.gui.VariableGridLayout;

import org.gjt.sp.jedit.menu.EnhancedCheckBoxMenuItem;
import org.gjt.sp.jedit.menu.EnhancedMenu;
import org.gjt.sp.jedit.menu.EnhancedMenuItem;

import org.gjt.sp.jedit.syntax.SyntaxStyle;
import org.gjt.sp.jedit.syntax.Token;

import org.gjt.sp.jedit.textarea.TextAreaMouseHandler;

import org.gjt.sp.util.Log;
//}}}

/**
 * Various GUI functions.<p>
 *
 * The most frequently used members of this class are:
 *
 * <ul>
 * <li>{@link #loadIcon(String)}</li>
 * <li>{@link #confirm(Component,String,Object[],int,int)}</li>
 * <li>{@link #error(Component,String,Object[])}</li>
 * <li>{@link #message(Component,String,Object[])}</li>

 * <li>{@link #showVFSFileDialog(View,String,int,boolean)}</li>
 * <li>{@link #loadGeometry(Window,String)}</li>
 * <li>{@link #saveGeometry(Window,String)}</li>
 * <li>{@link #showPopupMenu(JPopupMenu,Component,int,int)}</li>
 * </ul>
 *
 * @author Slava Pestov
 * @version $Id: GUIUtilities.java 9995 2007-07-10 23:22:40Z Vampire0 $
 */
public class GUIUtilities
{
	//{{{ Some predefined icons
	/**
	 * @deprecated Use <code>GUIUtilities.loadIcon("new.gif");</code>
	 * instead.
	 */
	public static Icon NEW_BUFFER_ICON;

	/**
	 * @deprecated Use <code>GUIUtilities.loadIcon("dirty.gif");</code>
	 * instead.
	 */
	public static Icon DIRTY_BUFFER_ICON;

	/**
	 * @deprecated Use <code>GUIUtilities.loadIcon("readonly.gif");</code>
	 * instead.
	 */
	public static Icon READ_ONLY_BUFFER_ICON;

	/**
	 * @deprecated Use <code>GUIUtilities.loadIcon("normal.gif");</code>
	 * instead.
	 */
	public static Icon NORMAL_BUFFER_ICON;

	/**
	 * @deprecated Use <code>GUIUtilities.loadIcon("jedit-icon.gif");</code>
	 * instead.
	 */
	public static Icon WINDOW_ICON;
	//}}}

	//{{{ Icon methods

	//{{{ setIconPath() method
	/**
	 * Sets the path where jEdit looks for icons.
	 * @since jEdit 4.2pre5
	 */
	public static void setIconPath(String iconPath)
	{
		GUIUtilities.iconPath = iconPath;
		if(icons != null)
			icons.clear();
	} //}}}

	//{{{ loadIcon() method
	/**
	 * Loads an icon.
	 * @param iconName The icon name
	 * @since jEdit 2.6pre7
	 */
	public static Icon loadIcon(String iconName)
	{
		if(icons == null)
			icons = new Hashtable<String, Icon>();

		// check if there is a cached version first
		Icon icon = icons.get(iconName);
		if(icon != null)
			return icon;

		URL url;

		try
		{
			// get the icon
			if(MiscUtilities.isURL(iconName))
				url = new URL(iconName);
			else
				url = new URL(iconPath + iconName);
		}
		catch(Exception e)
		{
			try
			{
				url = new URL(defaultIconPath + iconName);
			}
			catch(Exception ex)
			{
				Log.log(Log.ERROR,GUIUtilities.class,
					"Icon not found: " + iconName);
				Log.log(Log.ERROR,GUIUtilities.class,ex);
				return null;
			}
		}

		icon = new ImageIcon(url);

		icons.put(iconName,icon);
		return icon;
	} //}}}

	//{{{ getEditorIcon() method
	/**
	 * Returns the default editor window image.
	 */
	public static Image getEditorIcon()
	{
		return ((ImageIcon)loadIcon("jedit-icon.gif")).getImage();
	} //}}}

	//{{{ getPluginIcon() method
	/**
	 * Returns the default plugin window image.
	 */
	public static Image getPluginIcon()
	{
		return getEditorIcon();
	} //}}}

	//}}}

	//{{{ Menus, tool bars

	//{{{ loadMenuBar() method
	/**
	 * Creates a menubar. Plugins should not need to call this method.
	 * @param name The menu bar name
	 * @since jEdit 3.2pre5
	 */
	public static JMenuBar loadMenuBar(String name)
	{
		return loadMenuBar(jEdit.getActionContext(),name);
	} //}}}

	//{{{ loadMenuBar() method
	/**
	 * Creates a menubar. Plugins should not need to call this method.
	 * @param context An action context
	 * @param name The menu bar name
	 * @since jEdit 4.2pre1
	 */
	public static JMenuBar loadMenuBar(ActionContext context, String name)
	{
		String menus = jEdit.getProperty(name);
		StringTokenizer st = new StringTokenizer(menus);

		JMenuBar mbar = new JMenuBar();

		while(st.hasMoreTokens())
		{
			String menuName = st.nextToken();
			mbar.add(loadMenu(context, menuName));
		}

		return mbar;
	} //}}}

	//{{{ loadMenu() method
	/**
	 * Creates a menu. The menu label is set from the
	 * <code><i>name</i>.label</code> property. The menu contents is taken
	 * from the <code><i>name</i></code> property, which is a whitespace
	 * separated list of action names. An action name of <code>-</code>
	 * inserts a separator in the menu.
	 * @param name The menu name
	 * @see #loadMenuItem(String)
	 * @since jEdit 2.6pre2
	 */
	public static JMenu loadMenu(String name)
	{
		return loadMenu(jEdit.getActionContext(),name);
	} //}}}

	//{{{ loadMenu() method
	/**
	 * Creates a menu. The menu label is set from the
	 * <code><i>name</i>.label</code> property. The menu contents is taken
	 * from the <code><i>name</i></code> property, which is a whitespace
	 * separated list of action names. An action name of <code>-</code>
	 * inserts a separator in the menu.
	 * @param context An action context; either
	 * <code>jEdit.getActionContext()</code> or
	 * <code>VFSBrowser.getActionContext()</code>.
	 * @param name The menu name
	 * @see #loadMenuItem(String)
	 * @since jEdit 4.2pre1
	 */
	public static JMenu loadMenu(ActionContext context, String name)
	{
		return new EnhancedMenu(name,
			jEdit.getProperty(name.concat(".label")),
			context);
	} //}}}

	//{{{ loadPopupMenu() method
	/**
	 * Creates a popup menu.

	 * @param name The menu name
	 * @since jEdit 2.6pre2
	 */
	public static JPopupMenu loadPopupMenu(String name)
	{
		return loadPopupMenu(jEdit.getActionContext(),name);
	} //}}}

	//{{{ loadPopupMenu() method
	/**
	 * Creates a popup menu.

	 * @param context An action context; either
	 * <code>jEdit.getActionContext()</code> or
	 * <code>VFSBrowser.getActionContext()</code>.
	 * @param name The menu name
	 * @since jEdit 4.2pre1
	 */
	public static JPopupMenu loadPopupMenu(ActionContext context, String name)
	{
		JPopupMenu menu = new JPopupMenu();

		String menuItems = jEdit.getProperty(name);
		if(menuItems != null)
		{
			StringTokenizer st = new StringTokenizer(menuItems);
			while(st.hasMoreTokens())
			{
				String menuItemName = st.nextToken();
				if(menuItemName.equals("-"))
					menu.addSeparator();
				else
					menu.add(loadMenuItem(context,menuItemName,false));
			}
		}

		return menu;
	} //}}}

	//{{{ loadMenuItem() method
	/**
	 * Creates a menu item. The menu item is bound to the action named by
	 * <code>name</code> with label taken from the return value of the
	 * {@link EditAction#getLabel()} method.
	 *
	 * @param name The menu item name
	 * @see #loadMenu(String)
	 * @since jEdit 2.6pre1
	 */
	public static JMenuItem loadMenuItem(String name)
	{
		return loadMenuItem(jEdit.getActionContext(),name,true);
	} //}}}

	//{{{ loadMenuItem() method
	/**
	 * Creates a menu item.
	 * @param name The menu item name
	 * @param setMnemonic True if the menu item should have a mnemonic
	 * @since jEdit 3.1pre1
	 */
	public static JMenuItem loadMenuItem(String name, boolean setMnemonic)
	{
		return loadMenuItem(jEdit.getActionContext(),name,setMnemonic);
	} //}}}

	//{{{ loadMenuItem() method
	/**
	 * Creates a menu item.
	 * @param context An action context; either
	 * <code>jEdit.getActionContext()</code> or
	 * <code>VFSBrowser.getActionContext()</code>.
	 * @param name The menu item name
	 * @param setMnemonic True if the menu item should have a mnemonic
	 * @since jEdit 4.2pre1
	 */
	public static JMenuItem loadMenuItem(ActionContext context, String name,
		boolean setMnemonic)
	{
		if(name.charAt(0) == '%')
			return loadMenu(context,name.substring(1));

		String label = jEdit.getProperty(name + ".label");
		if(label == null)
			label = name;

		char mnemonic;
		int index = label.indexOf('$');
		if(index != -1 && label.length() - index > 1)
		{
			mnemonic = Character.toLowerCase(label.charAt(index + 1));
			label = label.substring(0,index).concat(label.substring(++index));
		}
		else
			mnemonic = '\0';

		JMenuItem mi;
		if(jEdit.getBooleanProperty(name + ".toggle"))
			mi = new EnhancedCheckBoxMenuItem(label,name,context);
		else
			mi = new EnhancedMenuItem(label,name,context);

		if(!OperatingSystem.isMacOS() && setMnemonic && mnemonic != '\0')
			mi.setMnemonic(mnemonic);

		return mi;
	} //}}}

	// {{{ loadMenuItem(EditAction, boolean)
	public static JMenuItem loadMenuItem(EditAction editAction,
		boolean setMnemonic)
	{
		String name = editAction.getName();
		ActionContext context = jEdit.getActionContext();

		// String label = jEdit.getProperty(name + ".label");
		String label = editAction.getLabel();
		if(label == null)
			label = name;

		char mnemonic;
		int index = label.indexOf('$');
		if(index != -1 && label.length() - index > 1)
		{
			mnemonic = Character.toLowerCase(label.charAt(index + 1));
			label = label.substring(0,index).concat(label.substring(++index));
		}
		else
			mnemonic = '\0';

		JMenuItem mi;
		if(jEdit.getBooleanProperty(name + ".toggle"))
			mi = new EnhancedCheckBoxMenuItem(label,name,context);
		else
			mi = new EnhancedMenuItem(label,name,context);

		if(!OperatingSystem.isMacOS() && setMnemonic && mnemonic != '\0')
			mi.setMnemonic(mnemonic);

		return mi;
	} //}}}

	//{{{ loadToolBar() method
	/**
	 * Creates a toolbar.
	 * @param name The toolbar name
	 * @since jEdit 4.2pre2
	 */
	public static Box loadToolBar(String name)
	{
		return loadToolBar(jEdit.getActionContext(),name);
	} //}}}

	//{{{ loadToolBar() method
	/**
	 * Creates a toolbar.
	 * @param context An action context; either
	 * <code>jEdit.getActionContext()</code> or
	 * <code>VFSBrowser.getActionContext()</code>.
	 * @param name The toolbar name
	 * @since jEdit 4.2pre2
	 */
	public static Box loadToolBar(ActionContext context, String name)
	{
		Box toolBar = new Box(BoxLayout.X_AXIS);

		String buttons = jEdit.getProperty(name);
		if(buttons != null)
		{
			StringTokenizer st = new StringTokenizer(buttons);
			while(st.hasMoreTokens())
			{
				String button = st.nextToken();
				if(button.equals("-"))
					toolBar.add(Box.createHorizontalStrut(12));
				else
				{
					JButton b = loadToolButton(context,button);
					if(b != null)
						toolBar.add(b);
				}
			}
		}

		toolBar.add(Box.createGlue());

		return toolBar;
	} //}}}

	//{{{ loadToolButton() method
	/**
	 * Loads a tool bar button. The tooltip is constructed from
	 * the <code><i>name</i>.label</code> and
	 * <code><i>name</i>.shortcut</code> properties and the icon is loaded
	 * from the resource named '/org/gjt/sp/jedit/icons/' suffixed
	 * with the value of the <code><i>name</i>.icon</code> property.
	 * @param name The name of the button
	 */
	public static EnhancedButton loadToolButton(String name)
	{
		return loadToolButton(jEdit.getActionContext(),name);
	} //}}}

	//{{{ loadToolButton() method
	/**
	 * Loads a tool bar button. The tooltip is constructed from
	 * the <code><i>name</i>.label</code> and
	 * <code><i>name</i>.shortcut</code> properties and the icon is loaded
	 * from the resource named '/org/gjt/sp/jedit/icons/' suffixed
	 * with the value of the <code><i>name</i>.icon</code> property.
	 * @param context An action context; either
	 * <code>jEdit.getActionContext()</code> or
	 * <code>VFSBrowser.getActionContext()</code>.
	 * @param name The name of the button
	 * @since jEdit 4.2pre1
	 */
	public static EnhancedButton loadToolButton(ActionContext context,
		String name)
	{
		String label = jEdit.getProperty(name + ".label");

		if(label == null)
			label = name;

		Icon icon;
		String iconName = jEdit.getProperty(name + ".icon");
		if(iconName == null)
			icon = loadIcon("BrokenImage.png");
		else
		{
			icon = loadIcon(iconName);
			if(icon == null)
				icon = loadIcon("BrokenImage.png");
		}

		String toolTip = prettifyMenuLabel(label);
		String shortcut1 = jEdit.getProperty(name + ".shortcut");
		String shortcut2 = jEdit.getProperty(name + ".shortcut2");
		if(shortcut1 != null || shortcut2 != null)
		{
			toolTip = toolTip + " ("
				+ (shortcut1 != null
				? shortcut1 : "")
				+ ((shortcut1 != null && shortcut2 != null)
				? " or " : "")
				+ (shortcut2 != null
				? shortcut2
				: "") + ')';
		}

		return new EnhancedButton(icon,toolTip,name,context);
	} //}}}

	//{{{ prettifyMenuLabel() method
	/**
	 * `Prettifies' a menu item label by removing the `$' sign. This
	 * can be used to process the contents of an <i>action</i>.label
	 * property.
	 */
	public static String prettifyMenuLabel(String label)
	{
		int index = label.indexOf('$');
		if(index != -1)
		{
			label = label.substring(0,index)
				.concat(label.substring(index + 1));
		}
		return label;
	} //}}}

	//}}}

	//{{{ Canned dialog boxes

	//{{{ message() method
	/**
	 * Displays a dialog box.
	 * The title of the dialog is fetched from
	 * the <code><i>name</i>.title</code> property. The message is fetched
	 * from the <code><i>name</i>.message</code> property. The message
	 * is formatted by the property manager with <code>args</code> as
	 * positional parameters.
	 * @param comp The component to display the dialog for
	 * @param name The name of the dialog
	 * @param args Positional parameters to be substituted into the
	 * message text
	 */
	public static void message(Component comp, String name, Object[] args)
	{
		hideSplashScreen();

		JOptionPane.showMessageDialog(comp,
			jEdit.getProperty(name.concat(".message"),args),
			jEdit.getProperty(name.concat(".title"),args),
			JOptionPane.INFORMATION_MESSAGE);
	} //}}}

	//{{{ error() method
	/**
	 * Displays an error dialog box.
	 * The title of the dialog is fetched from
	 * the <code><i>name</i>.title</code> property. The message is fetched
	 * from the <code><i>name</i>.message</code> property. The message
	 * is formatted by the property manager with <code>args</code> as
	 * positional parameters.
	 * @param comp The component to display the dialog for
	 * @param name The name of the dialog
	 * @param args Positional parameters to be substituted into the
	 * message text
	 */
	public static void error(Component comp, String name, Object[] args)
	{
		hideSplashScreen();

		JOptionPane.showMessageDialog(comp,
			jEdit.getProperty(name.concat(".message"),args),
			jEdit.getProperty(name.concat(".title"),args),
			JOptionPane.ERROR_MESSAGE);
	} //}}}

	//{{{ input() method
	/**
	 * Displays an input dialog box and returns any text the user entered.
	 * The title of the dialog is fetched from
	 * the <code><i>name</i>.title</code> property. The message is fetched
	 * from the <code><i>name</i>.message</code> property.
	 * @param comp The component to display the dialog for
	 * @param name The name of the dialog
	 * @param def The text to display by default in the input field
	 */
	public static String input(Component comp, String name, Object def)
	{
		return input(comp,name,null,def);
	} //}}}

	//{{{ inputProperty() method
	/**
	 * Displays an input dialog box and returns any text the user entered.
	 * The title of the dialog is fetched from
	 * the <code><i>name</i>.title</code> property. The message is fetched
	 * from the <code><i>name</i>.message</code> property.
	 * @param comp The component to display the dialog for
	 * @param name The name of the dialog
	 * @param def The property whose text to display in the input field
	 */
	public static String inputProperty(Component comp, String name,
		String def)
	{
		return inputProperty(comp,name,null,def);
	} //}}}

	//{{{ input() method
	/**
	 * Displays an input dialog box and returns any text the user entered.
	 * The title of the dialog is fetched from
	 * the <code><i>name</i>.title</code> property. The message is fetched
	 * from the <code><i>name</i>.message</code> property.
	 * @param comp The component to display the dialog for
	 * @param name The name of the dialog
	 * @param def The text to display by default in the input field
	 * @param args Positional parameters to be substituted into the
	 * message text
	 * @since jEdit 3.1pre3
	 */
	public static String input(Component comp, String name,
		Object[] args, Object def)
	{
		hideSplashScreen();

		String retVal = (String)JOptionPane.showInputDialog(comp,
			jEdit.getProperty(name.concat(".message"),args),
			jEdit.getProperty(name.concat(".title")),
			JOptionPane.QUESTION_MESSAGE,null,null,def);
		return retVal;
	} //}}}

	//{{{ inputProperty() method
	/**
	 * Displays an input dialog box and returns any text the user entered.
	 * The title of the dialog is fetched from
	 * the <code><i>name</i>.title</code> property. The message is fetched
	 * from the <code><i>name</i>.message</code> property.
	 * @param comp The component to display the dialog for
	 * @param name The name of the dialog
	 * @param args Positional parameters to be substituted into the
	 * message text
	 * @param def The property whose text to display in the input field
	 * @since jEdit 3.1pre3
	 */
	public static String inputProperty(Component comp, String name,
		Object[] args, String def)
	{
		hideSplashScreen();

		String retVal = (String)JOptionPane.showInputDialog(comp,
			jEdit.getProperty(name.concat(".message"),args),
			jEdit.getProperty(name.concat(".title")),
			JOptionPane.QUESTION_MESSAGE,
			null,null,jEdit.getProperty(def));
		if(retVal != null)
			jEdit.setProperty(def,retVal);
		return retVal;
	} //}}}

	//{{{ confirm() method
	/**
	 * Displays a confirm dialog box and returns the button pushed by the
	 * user. The title of the dialog is fetched from the
	 * <code><i>name</i>.title</code> property. The message is fetched
	 * from the <code><i>name</i>.message</code> property.
	 * @param comp The component to display the dialog for
	 * @param name The name of the dialog
	 * @param args Positional parameters to be substituted into the
	 * message text
	 * @param buttons The buttons to display - for example,
	 * JOptionPane.YES_NO_CANCEL_OPTION
	 * @param type The dialog type - for example,
	 * JOptionPane.WARNING_MESSAGE
	 * @since jEdit 3.1pre3
	 */
	public static int confirm(Component comp, String name,
		Object[] args, int buttons, int type)
	{
		hideSplashScreen();

		return JOptionPane.showConfirmDialog(comp,
			jEdit.getProperty(name + ".message",args),
			jEdit.getProperty(name + ".title"),buttons,type);
	} //}}}

	//{{{ listConfirm() method
	/**
	 * Displays a confirm dialog box and returns the button pushed by the
	 * user. The title of the dialog is fetched from the
	 * <code><i>name</i>.title</code> property. The message is fetched
	 * from the <code><i>name</i>.message</code> property. The dialog
	 * also shows a list of entries given by the <code>listModel</code>
	 * parameter.
	 * @since jEdit 4.3pre1
	 */
	public static int listConfirm(Component comp, String name, String[] args,
		Object[] listModel)
	{
		JList list = new JList(listModel);
		list.setVisibleRowCount(8);

		Object[] message = {
			jEdit.getProperty(name + ".message",args),
			new JScrollPane(list)
		};

		return JOptionPane.showConfirmDialog(comp,
			message,
			jEdit.getProperty(name + ".title"),
			JOptionPane.YES_NO_OPTION,
			JOptionPane.QUESTION_MESSAGE);
	} //}}}

	//{{{ showVFSFileDialog() methods
	/**
	 * Displays a VFS file selection dialog box.
	 * @param view The view, should be non-null
	 * @param path The initial directory to display. May be null
	 * @param type The dialog type. One of
	 * {@link org.gjt.sp.jedit.browser.VFSBrowser#OPEN_DIALOG},
	 * {@link org.gjt.sp.jedit.browser.VFSBrowser#SAVE_DIALOG}, or
	 * {@link org.gjt.sp.jedit.browser.VFSBrowser#CHOOSE_DIRECTORY_DIALOG}.
	 * @param multipleSelection True if multiple selection should be allowed
	 * @return The selected file(s)
	 * @since jEdit 2.6pre2
	 */
	public static String[] showVFSFileDialog(View view, String path,
		int type, boolean multipleSelection)
	{
		// the view should not be null, but some plugins might do this
		if(view == null)
		{
			Log.log(Log.WARNING,GUIUtilities.class,
			"showVFSFileDialog(): given null view, assuming jEdit.getActiveView()");
			view = jEdit.getActiveView();
		}

		hideSplashScreen();

		VFSFileChooserDialog fileChooser = new VFSFileChooserDialog(
			view,path,type,multipleSelection);
		return fileChooser.getSelectedFiles();
	} 

	/**
	 * Displays a VFS file selection dialog box.
	 * This version can specify a dialog as the parent instead
	 * of the view.
	 * @param view The view, should be non-null
	 * @param path The initial directory to display. May be null
	 * @param type The dialog type. One of
	 * {@link org.gjt.sp.jedit.browser.VFSBrowser#OPEN_DIALOG},
	 * {@link org.gjt.sp.jedit.browser.VFSBrowser#SAVE_DIALOG}, or
	 * {@link org.gjt.sp.jedit.browser.VFSBrowser#CHOOSE_DIRECTORY_DIALOG}.
	 * @param multipleSelection True if multiple selection should be allowed
	 * @return The selected file(s)	 
	 * @since jEdit 4.3pre10
	 */
	public static String[] showVFSFileDialog(Dialog parent, View view,
		String path, int type, boolean multipleSelection)
	{
		hideSplashScreen();

		VFSFileChooserDialog fileChooser = new VFSFileChooserDialog(
			parent, view, path, type, multipleSelection, true);
		return fileChooser.getSelectedFiles();
	} 

	/**
	 * Displays a VFS file selection dialog box.
	 * This version can specify a frame as the parent instead
	 * of the view.
	 * @param parent The parent frame
	 * @param view The view, should be non-null
	 * @param path The initial directory to display. May be null
	 * @param type The dialog type. One of
	 * {@link org.gjt.sp.jedit.browser.VFSBrowser#OPEN_DIALOG},
	 * {@link org.gjt.sp.jedit.browser.VFSBrowser#SAVE_DIALOG}, or
	 * {@link org.gjt.sp.jedit.browser.VFSBrowser#CHOOSE_DIRECTORY_DIALOG}.
	 * @param multipleSelection True if multiple selection should be allowed
	 * @return The selected file(s)	 
	 * @since jEdit 4.3pre10
	 */
	public static String[] showVFSFileDialog(Frame parent, View view,
		String path, int type, boolean multipleSelection)
	{
		hideSplashScreen();
		VFSFileChooserDialog fileChooser = new VFSFileChooserDialog(
			parent, view, path, type, multipleSelection, true);
		return fileChooser.getSelectedFiles();
	} //}}}

	//{{{ Colors and styles

	//{{{ parseColor() method
	/**
	 * Converts a color name to a color object. The name must either be
	 * a known string, such as `red', `green', etc (complete list is in
	 * the <code>java.awt.Color</code> class) or a hex color value
	 * prefixed with `#', for example `#ff0088'.
	 * @param name The color name
	 */
	public static Color parseColor(String name)
	{
		return parseColor(name, Color.black);
	} //}}}

	//{{{ parseColor() method
	public static Color parseColor(String name, Color defaultColor)
	{
		if(name == null)
			return defaultColor;
		else if(name.charAt(0) == '#')
		{
			try
			{
				return Color.decode(name);
			}
			catch(NumberFormatException nf)
			{
				return defaultColor;
			}
		}
		else if("red".equals(name))
			return Color.red;
		else if("green".equals(name))
			return Color.green;
		else if("blue".equals(name))
			return Color.blue;
		else if("yellow".equals(name))
			return Color.yellow;
		else if("orange".equals(name))
			return Color.orange;
		else if("white".equals(name))
			return Color.white;
		else if("lightGray".equals(name))
			return Color.lightGray;
		else if("gray".equals(name))
			return Color.gray;
		else if("darkGray".equals(name))
			return Color.darkGray;
		else if("black".equals(name))
			return Color.black;
		else if("cyan".equals(name))
			return Color.cyan;
		else if("magenta".equals(name))
			return Color.magenta;
		else if("pink".equals(name))
			return Color.pink;
		else
			return defaultColor;
	} //}}}

	//{{{ getColorHexString() method
	/**
	 * Converts a color object to its hex value. The hex value
	 * prefixed is with `#', for example `#ff0088'.
	 * @param c The color object
	 */
	public static String getColorHexString(Color c)
	{
		String colString = Integer.toHexString(c.getRGB() & 0xffffff);
		return "#000000".substring(0,7 - colString.length()).concat(colString);
	} //}}}

	//{{{ parseStyle() method
	/**
	 * Converts a style string to a style object.
	 * @param str The style string
	 * @param family Style strings only specify font style, not font family
	 * @param size Style strings only specify font style, not font family
	 * @exception IllegalArgumentException if the style is invalid
	 * @since jEdit 3.2pre6
	 */
	public static SyntaxStyle parseStyle(String str, String family, int size)
		throws IllegalArgumentException
	{
		return parseStyle(str,family,size,true);
	} //}}}

	//{{{ parseStyle() method
	/**
	 * Converts a style string to a style object.
	 * @param str The style string
	 * @param family Style strings only specify font style, not font family
	 * @param size Style strings only specify font style, not font family
	 * @param color If false, the styles will be monochrome
	 * @exception IllegalArgumentException if the style is invalid
	 * @since jEdit 4.0pre4
	 */
	public static SyntaxStyle parseStyle(String str, String family, int size,
		boolean color)
		throws IllegalArgumentException
	{
		Color fgColor = Color.black;
		Color bgColor = null;
		boolean italic = false;
		boolean bold = false;
		StringTokenizer st = new StringTokenizer(str);
		while(st.hasMoreTokens())
		{
			String s = st.nextToken();
			if(s.startsWith("color:"))
			{
				if(color)
					fgColor = GUIUtilities.parseColor(s.substring(6), Color.black);
			}
			else if(s.startsWith("bgColor:"))
			{
				if(color)
					bgColor = GUIUtilities.parseColor(s.substring(8), null);
			}
			else if(s.startsWith("style:"))
			{
				for(int i = 6; i < s.length(); i++)
				{
					if(s.charAt(i) == 'i')
						italic = true;
					else if(s.charAt(i) == 'b')
						bold = true;
					else
						throw new IllegalArgumentException(
							"Invalid style: " + s);
				}
			}
			else
				throw new IllegalArgumentException(
					"Invalid directive: " + s);
		}
		return new SyntaxStyle(fgColor,bgColor,
			new Font(family,
			(italic ? Font.ITALIC : 0) | (bold ? Font.BOLD : 0),
			size));
	} //}}}

	//{{{ getStyleString() method
	/**
	 * Converts a style into it's string representation.
	 * @param style The style
	 */
	public static String getStyleString(SyntaxStyle style)
	{
		StringBuilder buf = new StringBuilder();

		if (style.getForegroundColor() != null)
		{
			buf.append("color:").append(getColorHexString(style.getForegroundColor()));
		}

		if (style.getBackgroundColor() != null)
		{
			buf.append(" bgColor:").append(getColorHexString(style.getBackgroundColor()));
		}

		Font font = style.getFont();
		if (!font.isPlain())
		{
			buf.append(" style:");
			if (font.isItalic())
				buf.append('i');
			if (font.isBold())
				buf.append('b');
		}

		return buf.toString();
	} //}}}

	//{{{ loadStyles() method
	/**
	 * Loads the syntax styles from the properties, giving them the specified
	 * base font family and size.
	 * @param family The font family
	 * @param size The font size
	 * @since jEdit 3.2pre6
	 */
	public static SyntaxStyle[] loadStyles(String family, int size)
	{
		return loadStyles(family,size,true);
	} //}}}

	//{{{ loadStyles() method
	/**
	 * Loads the syntax styles from the properties, giving them the specified
	 * base font family and size.
	 * @param family The font family
	 * @param size The font size
	 * @param color If false, the styles will be monochrome
	 * @since jEdit 4.0pre4
	 */
	public static SyntaxStyle[] loadStyles(String family, int size, boolean color)
	{
		SyntaxStyle[] styles = new SyntaxStyle[Token.ID_COUNT];

		// start at 1 not 0 to skip Token.NULL
		for(int i = 1; i < styles.length; i++)
		{
			try
			{
				String styleName = "view.style."
					+ Token.tokenToString((byte)i)
					.toLowerCase(Locale.ENGLISH);
				styles[i] = GUIUtilities.parseStyle(
					jEdit.getProperty(styleName),
					family,size,color);
			}
			catch(Exception e)
			{
				Log.log(Log.ERROR,GUIUtilities.class,e);
			}
		}

		return styles;
	} //}}}

	//}}}

	//{{{ Loading, saving window geometry

	//{{{ loadGeometry() method
	/**
	 * Loads a windows's geometry from the properties.
	 * The geometry is loaded from the <code><i>name</i>.x</code>,
	 * <code><i>name</i>.y</code>, <code><i>name</i>.width</code> and
	 * <code><i>name</i>.height</code> properties.
	 *
	 * @param win The window to load geometry from
	 * @param parent The parent frame to be relative to.
	 * @param name The name of the window
	 */
	public static void loadGeometry(Window win, Container parent, String name ) {
		int x, y, width, height;

		Dimension size = win.getSize();
		width = jEdit.getIntegerProperty(name + ".width", size.width);
		height = jEdit.getIntegerProperty(name + ".height", size.height);
		x = jEdit.getIntegerProperty(name + ".x",50);
		y = jEdit.getIntegerProperty(name + ".y",50);
		if(parent != null)
		{
			Point location = parent.getLocation();
			x = location.x + x;
			y = location.y + y;
		}

		int extState = jEdit.getIntegerProperty(name + ".extendedState", Frame.NORMAL);

		Rectangle desired = new Rectangle(x,y,width,height);
		try
		{
			if(!Debug.DISABLE_MULTIHEAD)
				adjustForScreenBounds(desired);
		}
		catch(Exception e)
		{
			/* Workaround for OS X bug. */
			Log.log(Log.ERROR,GUIUtilities.class,e);
		}

		if(OperatingSystem.isX11() && Debug.GEOMETRY_WORKAROUND)
			new UnixWorkaround(win,name,desired,extState);
		else
		{
			win.setBounds(desired);
			if(win instanceof Frame)
				((Frame)win).setExtendedState(extState);
		}

	} //}}}

	//{{{ loadGeometry() method
	/**
	 * Loads a windows's geometry from the properties.
	 * The geometry is loaded from the <code><i>name</i>.x</code>,
	 * <code><i>name</i>.y</code>, <code><i>name</i>.width</code> and
	 * <code><i>name</i>.height</code> properties.
	 *
	 * @param win The window to load geometry from
	 * @param name The name of the window
	 */
	public static void loadGeometry(Window win, String name)
	{
		loadGeometry(win, win.getParent(), name);
	} //}}}

	//{{{ adjustForScreenBounds() method
	/**
	 * Gives a rectangle the specified bounds, ensuring it is within the
	 * screen bounds.
	 * @since jEdit 4.2pre3
	 */
	public static void adjustForScreenBounds(Rectangle desired)
	{
		// Make sure the window is displayed in visible region
		Rectangle osbounds = OperatingSystem.getScreenBounds(desired);

		if(desired.x < osbounds.x || desired.x+desired.width
			> desired.x + osbounds.width)
		{
			if (desired.width > osbounds.width)
				desired.width = osbounds.width;
			desired.x = (osbounds.width - desired.width) / 2;
		}
		if(desired.y < osbounds.y || desired.y+desired.height
			> osbounds.y + osbounds.height)
		{
			if (desired.height >= osbounds.height)
				desired.height = osbounds.height;
			desired.y = (osbounds.height - desired.height) / 2;
		}
	} //}}}

	//{{{ UnixWorkaround class
	static class UnixWorkaround
	{
		Window win;
		String name;
		Rectangle desired;
		Rectangle required;
		long start;
		boolean windowOpened;

		//{{{ UnixWorkaround constructor
		UnixWorkaround(Window win, String name, Rectangle desired,
			int extState)
		{
			this.win = win;
			this.name = name;
			this.desired = desired;

			int adjust_x, adjust_y, adjust_width, adjust_height;
			adjust_x = jEdit.getIntegerProperty(name + ".dx",0);
			adjust_y = jEdit.getIntegerProperty(name + ".dy",0);
			adjust_width = jEdit.getIntegerProperty(name + ".d-width",0);
			adjust_height = jEdit.getIntegerProperty(name + ".d-height",0);

			required = new Rectangle(
				desired.x - adjust_x,
				desired.y - adjust_y,
				desired.width - adjust_width,
				desired.height - adjust_height);

			Log.log(Log.DEBUG,GUIUtilities.class,"Window " + name
				+ ": desired geometry is " + desired);
			Log.log(Log.DEBUG,GUIUtilities.class,"Window " + name
				+ ": setting geometry to " + required);

			start = System.currentTimeMillis();

			win.setBounds(required);
			if(win instanceof Frame)
				((Frame)win).setExtendedState(extState);

			win.addComponentListener(new ComponentHandler());
			win.addWindowListener(new WindowHandler());
		} //}}}

		//{{{ ComponentHandler class
		class ComponentHandler extends ComponentAdapter
		{
			//{{{ componentMoved() method
			public void componentMoved(ComponentEvent evt)
			{
				if(System.currentTimeMillis() - start < 1000L)
				{
					Rectangle r = win.getBounds();
					if(!windowOpened && r.equals(required))
						return;

					if(!r.equals(desired))
					{
						Log.log(Log.DEBUG,GUIUtilities.class,
							"Window resize blocked: " + win.getBounds());
						win.setBounds(desired);
					}
				}

				win.removeComponentListener(this);
			} //}}}

			//{{{ componentResized() method
			public void componentResized(ComponentEvent evt)
			{
				if(System.currentTimeMillis() - start < 1000L)
				{
					Rectangle r = win.getBounds();
					if(!windowOpened && r.equals(required))
						return;

					if(!r.equals(desired))
					{
 						Log.log(Log.DEBUG,GUIUtilities.class,
 							"Window resize blocked: " + win.getBounds());
						win.setBounds(desired);
					}
				}

				win.removeComponentListener(this);
			} //}}}
		} //}}}

		//{{{ WindowHandler class
		class WindowHandler extends WindowAdapter
		{
			//{{{ windowOpened() method
			public void windowOpened(WindowEvent evt)
			{
				windowOpened = true;

				Rectangle r = win.getBounds();
 				Log.log(Log.DEBUG,GUIUtilities.class,"Window "
 					+ name + ": bounds after opening: " + r);

				jEdit.setIntegerProperty(name + ".dx",
					r.x - required.x);
				jEdit.setIntegerProperty(name + ".dy",
					r.y - required.y);
				jEdit.setIntegerProperty(name + ".d-width",
					r.width - required.width);
				jEdit.setIntegerProperty(name + ".d-height",
					r.height - required.height);

				win.removeWindowListener(this);
			} //}}}
		} //}}}
	} //}}}

	//{{{ saveGeometry() method
	/**
	 * Saves a window's geometry to the properties.
	 * The geometry is saved to the <code><i>name</i>.x</code>,
	 * <code><i>name</i>.y</code>, <code><i>name</i>.width</code> and
	 * <code><i>name</i>.height</code> properties.<br />
	 * For Frame's and descendents use {@link #addSizeSaver(Frame,String)} to save the sizes
	 * correct even if the Frame is in maximized or iconified state.
	 * @param win The window to load geometry from
	 * @param name The name of the window
	 * @see #addSizeSaver(Frame,String)
	 */
	public static void saveGeometry(Window win, String name) {
		saveGeometry (win, win.getParent(), name);
	} //}}}

	//{{{ saveGeometry() method
	/**
	 * Saves a window's geometry to the properties.
	 * The geometry is saved to the <code><i>name</i>.x</code>,
	 * <code><i>name</i>.y</code>, <code><i>name</i>.width</code> and
	 * <code><i>name</i>.height</code> properties.<br />
	 * For Frame's and descendents use {@link #addSizeSaver(Frame,Container,String)} to save the sizes
	 * correct even if the Frame is in maximized or iconified state.
	 * @param win The window to load geometry from
	 * @param parent The parent frame to be relative to.
	 * @param name The name of the window
	 * @see #addSizeSaver(Frame,Container,String)
	 */
	public static void saveGeometry(Window win, Container parent, String name)
	{
		if(win instanceof Frame)
		{
			jEdit.setIntegerProperty(name + ".extendedState",
				((Frame)win).getExtendedState());
		}

		Rectangle bounds = win.getBounds();
		int x = bounds.x;
		int y = bounds.y;
		if (parent != null)
		{
			Rectangle parentBounds = parent.getBounds();
			x = x - parentBounds.x;
			y = y - parentBounds.y;
		}
		jEdit.setIntegerProperty(name + ".x",x);
		jEdit.setIntegerProperty(name + ".y",y);
		jEdit.setIntegerProperty(name + ".width", bounds.width);
		jEdit.setIntegerProperty(name + ".height", bounds.height);
	} //}}}

	//{{{ centerOnScreen() method
	/**
	 * Centers the given window on the screen. This method is needed because
	 * JDK 1.3 does not have a <code>JWindow.setLocationRelativeTo()</code>
	 * method.
	 * @since jEdit 4.2pre3
	 * @deprecated use {@link javax.swing.JWindow#setLocationRelativeTo(java.awt.Component)}
	 */
	@Deprecated
	public static void centerOnScreen(Window win)
	{
		GraphicsDevice gd = GraphicsEnvironment
			.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice();
		Rectangle gcbounds = gd.getDefaultConfiguration().getBounds();
		int x = gcbounds.x + (gcbounds.width - win.getWidth()) / 2;
		int y = gcbounds.y + (gcbounds.height - win.getHeight()) / 2;
		win.setLocation(x,y);
	} //}}}

	//}}}

	//{{{ hideSplashScreen() method
	/**
	 * Ensures that the splash screen is not visible. This should be
	 * called before displaying any dialog boxes or windows at
	 * startup.
	 */
	public static void hideSplashScreen()
	{
		if(splash != null)
		{
			splash.dispose();
			splash = null;
		}
	} //}}}

	//{{{ createMultilineLabel() method
	/**
	 * Creates a component that displays a multiple line message. This
	 * is implemented by assembling a number of <code>JLabels</code> in
	 * a <code>JPanel</code>.
	 * @param str The string, with lines delimited by newline
	 * (<code>\n</code>) characters.
	 * @since jEdit 4.1pre3
	 */
	public static JComponent createMultilineLabel(String str)
	{
		JPanel panel = new JPanel(new VariableGridLayout(
			VariableGridLayout.FIXED_NUM_COLUMNS,1,1,1));
		int lastOffset = 0;
		while(true)
		{
			int index = str.indexOf('\n',lastOffset);
			if(index == -1)
				break;
			else
			{
				panel.add(new JLabel(str.substring(lastOffset,index)));
				lastOffset = index + 1;
			}
		}

		if(lastOffset != str.length())
			panel.add(new JLabel(str.substring(lastOffset)));

		return panel;
	} //}}}

	//{{{ requestFocus() method
	/**
	 * Focuses on the specified component as soon as the window becomes
	 * active.
	 * @param win The window
	 * @param comp The component
	 */
	public static void requestFocus(final Window win, final Component comp)
	{
		win.addWindowFocusListener(new WindowAdapter()
		{
			public void windowGainedFocus(WindowEvent evt)
			{
				SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							comp.requestFocusInWindow();
						}
				});
				win.removeWindowFocusListener(this);
			}
		});
	} //}}}

	//{{{ isPopupTrigger() method
	/**
	 * Returns if the specified event is the popup trigger event.
	 * This implements precisely defined behavior, as opposed to
	 * MouseEvent.isPopupTrigger().
	 * @param evt The event
	 * @since jEdit 3.2pre8
	 */
	public static boolean isPopupTrigger(MouseEvent evt)
	{
		return TextAreaMouseHandler.isRightButton(evt.getModifiers());
	} //}}}

	//{{{ isMiddleButton() method
	/**
	 * @param modifiers The modifiers flag from a mouse event
	 * @since jEdit 4.1pre9
	 */
	public static boolean isMiddleButton(int modifiers)
	{
		return TextAreaMouseHandler.isMiddleButton(modifiers);
	} //}}}

	//{{{ isRightButton() method
	/**
	 * @param modifiers The modifiers flag from a mouse event
	 * @since jEdit 4.1pre9
	 */
	public static boolean isRightButton(int modifiers)
	{
		return TextAreaMouseHandler.isRightButton(modifiers);
	} //}}}

	//{{{ showPopupMenu() method
	/**
	 * Shows the specified popup menu, ensuring it is displayed within
	 * the bounds of the screen.
	 * @param popup The popup menu
	 * @param comp The component to show it for
	 * @param x The x co-ordinate
	 * @param y The y co-ordinate
	 * @since jEdit 4.0pre1
	 * @see {@link javax.swing.JComponent#setComponentPopupMenu(javax.swing.JPopupMenu)},
	 * which works better and is simpler to use: you don't have to write the code to
	 * show/hide popups in response to mouse events anymore.
	 */
	public static void showPopupMenu(JPopupMenu popup, Component comp,
		int x, int y)
	{
		showPopupMenu(popup,comp,x,y,true);
	} //}}}

	//{{{ showPopupMenu() method
	/**
	 * Shows the specified popup menu, ensuring it is displayed within
	 * the bounds of the screen.
	 * @param popup The popup menu
	 * @param comp The component to show it for
	 * @param x The x co-ordinate
	 * @param y The y co-ordinate
	 * @param point If true, then the popup originates from a single point;
	 * otherwise it will originate from the component itself. This affects
	 * positioning in the case where the popup does not fit onscreen.
	 *
	 * @since jEdit 4.1pre1
	 */
	public static void showPopupMenu(JPopupMenu popup, Component comp,
		int x, int y, boolean point)
	{
		int offsetX = 0;
		int offsetY = 0;

		int extraOffset = (point ? 1 : 0);

		Component win = comp;
		while(!(win instanceof Window || win == null))
		{
			offsetX += win.getX();
			offsetY += win.getY();
			win = win.getParent();
		}

		if(win != null)
		{
			Dimension size = popup.getPreferredSize();

			Rectangle screenSize = new Rectangle();

			GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();

			GraphicsDevice[] devices = ge.getScreenDevices();

			for (int j = 0; j < devices.length; j++)
			{
				GraphicsDevice device = devices[j];

				GraphicsConfiguration[] gc =
					device.getConfigurations();

				for (int i=0; i < gc.length; i++)
				{
					screenSize =
						screenSize.union(
							gc[i].getBounds());
				}
			}

			if(x + offsetX + size.width + win.getX() > screenSize.width
				&& x + offsetX + win.getX() >= size.width)
			{
				//System.err.println("x overflow");
				if(point)
					x -= (size.width + extraOffset);
				else
					x = (win.getWidth() - size.width - offsetX + extraOffset);
			}
			else
			{
				x += extraOffset;
			}

			//System.err.println("y=" + y + ",offsetY=" + offsetY
			//	+ ",size.height=" + size.height
			//	+ ",win.height=" + win.getHeight());
			if(y + offsetY + size.height + win.getY() > screenSize.height
				&& y + offsetY + win.getY() >= size.height)
			{
				if(point)
					y = (win.getHeight() - size.height - offsetY + extraOffset);
				else
					y = -size.height - 1;
			}
			else
			{
				y += extraOffset;
			}

			popup.show(comp,x,y);
		}
		else
			popup.show(comp,x + extraOffset,y + extraOffset);

	} //}}}

	//{{{ isAncestorOf() method
	/**
	 * Returns if the first component is an ancestor of the
	 * second by traversing up the component hierarchy.
	 *
	 * @param comp1 The ancestor
	 * @param comp2 The component to check
	 * @since jEdit 4.1pre5
	 */
	public static boolean isAncestorOf(Component comp1, Component comp2)
	{
		while(comp2 != null)
		{
			if(comp1 == comp2)
				return true;
			else
				comp2 = comp2.getParent();
		}

		return false;
	} //}}}

	//{{{ getParentDialog() method
	/**
	 * Traverses the given component's parent tree looking for an
	 * instance of JDialog, and return it. If not found, return null.
	 * @param c The component
	 */
	public static JDialog getParentDialog(Component c)
	{
		return (JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, c);
	} //}}}

	//{{{ getComponentParent() method
	/**
	 * Finds a parent of the specified component.
	 * @param comp The component
	 * @param clazz Looks for a parent with this class (exact match, not
	 * derived).
	 * @since jEdit 4.2pre1
	 */
	public static Component getComponentParent(Component comp, Class clazz)
	{
		while(true)
		{
			if(comp == null)
				break;

			if(comp instanceof JComponent)
			{
				Component real = (Component)((JComponent)comp)
					.getClientProperty("KORTE_REAL_FRAME");
				if(real != null)
					comp = real;
			}

			if(comp.getClass().equals(clazz))
				return comp;
			else if(comp instanceof JPopupMenu)
				comp = ((JPopupMenu)comp).getInvoker();
			else if(comp instanceof FloatingWindowContainer)
			{
				comp = ((FloatingWindowContainer)comp)
					.getDockableWindowManager();
			}
			else
				comp = comp.getParent();
		}
		return null;
	} //}}}

	//{{{ getView() method
	/**
	 * Finds the view parent of the specified component.
	 * @since jEdit 4.0pre2
	 */
	public static View getView(Component comp)
	{
		return (View)getComponentParent(comp,View.class);
	} //}}}

	//{{{ addSizeSaver() method
	/**
	* Adds a SizeSaver to the specified Frame. For non-Frame's use {@link #saveGeometry(Window,String)}
	 *
	 * @param frame The Frame for which to save the size
	 * @param name The prefix for the settings
	 * @since jEdit 4.3pre6
	 * @see #saveGeometry(Window,String)
	 */
	public static void addSizeSaver(Frame frame, String name)
	{
		addSizeSaver(frame,frame.getParent(),name);
	} //}}}

	//{{{ addSizeSaver() method
	/**
	 * Adds a SizeSaver to the specified Frame. For non-Frame's use {@link #saveGeometry(Window,Container,String)}
	 *
	 * @param frame The Frame for which to save the size
	 * @param parent The parent to be relative to
	 * @param name The prefix for the settings
	 * @since jEdit 4.3pre7
	 * @see #saveGeometry(Window,Container,String)
	 */
	public static void addSizeSaver(Frame frame, Container parent, String name)
	{
		SizeSaver ss = new SizeSaver(frame,parent,name);
		frame.addWindowStateListener(ss);
		frame.addComponentListener(ss);
	} //}}}

	//{{{ initContinuousLayout() method
	/**
	 * Init the continuous layout flag using the jEdit's property
	 * appearance.continuousLayout
	 *
	 * @param split the split. It must never be null
	 * @since jEdit 4.3pre9
	 */
	public static void initContinuousLayout(JSplitPane split)
	{
		boolean continuousLayout = split.isContinuousLayout();
		if (continuousLayout != jEdit.getBooleanProperty("appearance.continuousLayout"))
			split.setContinuousLayout(!continuousLayout);
	} //}}}

	//{{{ Package-private members

	//{{{ init() method
	static void init()
	{
		// don't do this in static{} since we need jEdit.initMisc()
		// run first so we have the jeditresource: protocol
		NEW_BUFFER_ICON = loadIcon("new.gif");
		DIRTY_BUFFER_ICON = loadIcon("dirty.gif");
		READ_ONLY_BUFFER_ICON = loadIcon("readonly.gif");
		NORMAL_BUFFER_ICON = loadIcon("normal.gif");
		WINDOW_ICON = loadIcon("jedit-icon.gif");
	} //}}}

	//{{{ showSplashScreen() method
	static void showSplashScreen()
	{
		splash = new SplashScreen();
	} //}}}

	//{{{ advanceSplashProgress() method
	static void advanceSplashProgress()
	{
		if(splash != null)
			splash.advance();
	} //}}}

	//{{{ advanceSplashProgress() method
	static void advanceSplashProgress(String label)
	{
		if(splash != null)
			splash.advance(label);
	} //}}}

	//}}}

	//{{{ Private members
	private static SplashScreen splash;
	private static Map<String, Icon> icons;
	private static String iconPath = "jeditresource:/org/gjt/sp/jedit/icons/";
	private static String defaultIconPath = "jeditresource:/org/gjt/sp/jedit/icons/";

	private GUIUtilities() {}
	//}}}

	//{{{ Inner classes

	//{{{ SizeSaver class
	/**
	 * A combined ComponentListener and WindowStateListener to continually save a Frames size.<br />
	 * For non-Frame's use {@link #saveGeometry(Window,String)}
	 *
	 * @author Björn Kautler
	 * @version $Id: GUIUtilities.java 9995 2007-07-10 23:22:40Z Vampire0 $
	 * @since jEdit 4.3pre6
	 * @see #saveGeometry(Window,Container,String)
	 */
	static class SizeSaver extends ComponentAdapter implements WindowStateListener
	{
		private Frame frame;
		private Container parent;
		private String name;

		//{{{ SizeSaver constructor
		/**
		 * Constructs a new SizeSaver.
		 *
		 * @param frame The Frame for which to save the size
		 * @param name The prefix for the settings
		 */
		SizeSaver(Frame frame, String name)
		{
			this.frame = frame;
			this.parent = frame.getParent();
		} //}}}

		//{{{ SizeSaver constructor
		/**
		 * Constructs a new SizeSaver.
		 *
		 * @param frame The Frame for which to save the size
		 * @param parent The parent to be relative to.
		 * @param name The prefix for the settings
		 */
		public SizeSaver(Frame frame, Container parent, String name)
		{
			if ((null == frame) || (null == name))
			{
				throw new NullPointerException();
			}
			this.frame = frame;
			this.parent = parent;
			this.name = name;
		} //}}}

		//{{{ windowStateChanged() method
		public void windowStateChanged(WindowEvent wse)
		{
			int extendedState = wse.getNewState();
			jEdit.setIntegerProperty(name + ".extendedState",extendedState);
			Rectangle bounds = frame.getBounds();
			save(extendedState, bounds);
		} //}}}

		//{{{ save() method
		private void save(int extendedState, Rectangle bounds)
		{
			switch (extendedState)
			{
				case Frame.MAXIMIZED_VERT:
					jEdit.setIntegerProperty(name + ".x",bounds.x);
					jEdit.setIntegerProperty(name + ".width",bounds.width);
					break;

				case Frame.MAXIMIZED_HORIZ:
					jEdit.setIntegerProperty(name + ".y",bounds.y);
					jEdit.setIntegerProperty(name + ".height",bounds.height);
					break;

				case Frame.NORMAL:
					saveGeometry(frame,parent,name );
					break;
			}
		} //}}}

		//{{{ componentResized() method
		public void componentResized(ComponentEvent ce)
		{
			componentMoved(ce);
		} //}}}

		//{{{ componentMoved() method
		public void componentMoved(ComponentEvent ce)
		{
			final Rectangle bounds = frame.getBounds();
			final Runnable sizeSaver = new Runnable()
			{
				public void run()
				{
					int extendedState = frame.getExtendedState();
					save(extendedState, bounds);
				}
			};
			new Thread("Sizesavingdelay")
			{
				public void run()
				{
					try
					{
						Thread.sleep(500L);
					}
					catch (InterruptedException ie)
					{
					}
					SwingUtilities.invokeLater(sizeSaver);
				}
			}.start();
		} //}}}
	} //}}}

	//}}}
}
