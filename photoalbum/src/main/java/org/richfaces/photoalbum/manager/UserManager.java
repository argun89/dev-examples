/**
 * License Agreement.
 *
 *  JBoss RichFaces - Ajax4jsf Component Library
 *
 * Copyright (C) 2007  Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */
package org.richfaces.photoalbum.manager;

/**
 * Class encapsulated all functionality, related to working with user.
 *
 * @author Andrey Markhel
 */
import java.io.File;
import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.richfaces.photoalbum.domain.User;
import org.richfaces.photoalbum.event.AlbumEvent;
import org.richfaces.photoalbum.event.EventType;
import org.richfaces.photoalbum.event.Events;
import org.richfaces.photoalbum.event.SimpleEvent;
import org.richfaces.photoalbum.service.Constants;
import org.richfaces.photoalbum.service.IUserAction;

@RequestScoped
public class UserManager implements Serializable {

    private static final long serialVersionUID = 6027103521084558931L;

    // @In(scope=ScopeType.SESSION) @Out(scope=ScopeType.SESSION)
    @Inject
    User user;

    @Inject
    FileManager fileManager;

    // @In(required=false, scope=ScopeType.CONVERSATION) @Out(required=false, scope=ScopeType.CONVERSATION)
    @Inject
    File avatarData;

    @Inject
    IUserAction userAction;

    @Inject
    @EventType(Events.ADD_ERROR_EVENT)
    Event<SimpleEvent> error;

    /**
     * Method, that invoked when user want to edit her profile.
     *
     */
    public void editUser(@Observes @EventType(Events.EDIT_USER_EVENT) SimpleEvent se) {
        // If new avatar was uploaded
        if (avatarData != null) {
            if (!fileManager.saveAvatar(avatarData, user)) {
                error.fire(new SimpleEvent(Constants.FILE_IO_ERROR));
                return;
            }
            avatarData.delete();
            avatarData = null;
            user.setHasAvatar(true);
        }
        try {
            // This check is actual only on livedemo server to prevent hacks.
            // Prevent hackers to mark user as pre-defined
            user.setPreDefined(false);
            // user.setPasswordHash(HashUtils.hash(user.getPassword()));
            user = userAction.updateUser();
        } catch (Exception e) {
            error.fire(new SimpleEvent(Constants.UPDATE_USER_ERROR));
            return;
        }
    }

    /**
     * This method observes <code>Constants.ALBUM_ADDED_EVENT</code> and invoked after the user add new album
     *
     * @param album - added album
     */
    public void onAlbumAdded(@Observes @EventType(Events.ALBUM_ADDED_EVENT) AlbumEvent ae) {
        user = userAction.refreshUser();
    }

    /**
     * Method, that invoked when user click 'Cancel' button during edit her profile.
     *
     */
    public void cancelEditUser(@Observes @EventType(Events.CANCEL_EDIT_USER_EVENT) SimpleEvent se) {
        avatarData = null;
    }
}