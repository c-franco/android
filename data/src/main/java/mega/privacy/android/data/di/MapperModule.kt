package mega.privacy.android.data.di

import android.webkit.MimeTypeMap
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mega.privacy.android.data.mapper.AccountDetailMapper
import mega.privacy.android.data.mapper.AccountLevelDetailMapper
import mega.privacy.android.data.mapper.AccountSessionDetailMapper
import mega.privacy.android.data.mapper.AccountSessionMapper
import mega.privacy.android.data.mapper.AccountStorageDetailMapper
import mega.privacy.android.data.mapper.AccountTransferDetailMapper
import mega.privacy.android.data.mapper.AccountTypeMapper
import mega.privacy.android.data.mapper.AchievementsOverviewMapper
import mega.privacy.android.data.mapper.BooleanPreferenceMapper
import mega.privacy.android.data.mapper.ChatFilesFolderUserAttributeMapper
import mega.privacy.android.data.mapper.ChatListItemMapper
import mega.privacy.android.data.mapper.ChatRequestMapper
import mega.privacy.android.data.mapper.ChatRoomMapper
import mega.privacy.android.data.mapper.ChatScheduledMeetingMapper
import mega.privacy.android.data.mapper.ChatScheduledMeetingMapperImpl
import mega.privacy.android.data.mapper.ChatScheduledMeetingOccurrMapper
import mega.privacy.android.data.mapper.ChatScheduledMeetingOccurrMapperImpl
import mega.privacy.android.data.mapper.CombinedChatRoomMapper
import mega.privacy.android.data.mapper.CombinedChatRoomMapperImpl
import mega.privacy.android.data.mapper.ContactCredentialsMapper
import mega.privacy.android.data.mapper.ContactRequestMapper
import mega.privacy.android.data.mapper.CountryCallingCodeMapper
import mega.privacy.android.data.mapper.CountryMapper
import mega.privacy.android.data.mapper.CurrencyMapper
import mega.privacy.android.data.mapper.EventMapper
import mega.privacy.android.data.mapper.FileDurationMapper
import mega.privacy.android.data.mapper.FileTypeInfoMapper
import mega.privacy.android.data.mapper.FolderLoginStatusMapper
import mega.privacy.android.data.mapper.HandleListMapper
import mega.privacy.android.data.mapper.HandleListMapperImpl
import mega.privacy.android.data.mapper.ImageMapper
import mega.privacy.android.data.mapper.InviteContactRequestMapper
import mega.privacy.android.data.mapper.LocalPricingMapper
import mega.privacy.android.data.mapper.MediaStoreFileTypeMapper
import mega.privacy.android.data.mapper.MediaStoreFileTypeUriMapper
import mega.privacy.android.data.mapper.MegaAchievementMapper
import mega.privacy.android.data.mapper.MegaChatPeerListMapper
import mega.privacy.android.data.mapper.MegaExceptionMapper
import mega.privacy.android.data.mapper.MegaPurchaseMapper
import mega.privacy.android.data.mapper.MegaShareMapper
import mega.privacy.android.data.mapper.MegaSkuMapper
import mega.privacy.android.data.mapper.MegaTransferMapper
import mega.privacy.android.data.mapper.MimeTypeMapper
import mega.privacy.android.data.mapper.MyAccountCredentialsMapper
import mega.privacy.android.data.mapper.NodeMapper
import mega.privacy.android.data.mapper.NodeUpdateMapper
import mega.privacy.android.data.mapper.OfflineNodeInformationMapper
import mega.privacy.android.data.mapper.OnlineStatusMapper
import mega.privacy.android.data.mapper.PaymentMethodTypeMapper
import mega.privacy.android.data.mapper.PaymentPlatformTypeMapper
import mega.privacy.android.data.mapper.PricingMapper
import mega.privacy.android.data.mapper.RecentActionBucketMapper
import mega.privacy.android.data.mapper.RecentActionsMapper
import mega.privacy.android.data.mapper.ScannedContactLinkResultMapper
import mega.privacy.android.data.mapper.SortOrderIntMapper
import mega.privacy.android.data.mapper.SortOrderIntMapperImpl
import mega.privacy.android.data.mapper.SortOrderMapper
import mega.privacy.android.data.mapper.SortOrderMapperImpl
import mega.privacy.android.data.mapper.StartScreenMapper
import mega.privacy.android.data.mapper.StorageStateIntMapper
import mega.privacy.android.data.mapper.StorageStateMapper
import mega.privacy.android.data.mapper.SubscriptionOptionListMapper
import mega.privacy.android.data.mapper.SubscriptionStatusMapper
import mega.privacy.android.data.mapper.SyncStatusIntMapper
import mega.privacy.android.data.mapper.TransferEventMapper
import mega.privacy.android.data.mapper.UserAccountMapper
import mega.privacy.android.data.mapper.UserAlertMapper
import mega.privacy.android.data.mapper.UserCredentialsMapper
import mega.privacy.android.data.mapper.UserLastGreenMapper
import mega.privacy.android.data.mapper.UserSetMapper
import mega.privacy.android.data.mapper.UserUpdateMapper
import mega.privacy.android.data.mapper.VideoAttachmentMapper
import mega.privacy.android.data.mapper.VideoMapper
import mega.privacy.android.data.mapper.VideoQualityIntMapper
import mega.privacy.android.data.mapper.VideoQualityMapper
import mega.privacy.android.data.mapper.camerauploads.CameraUploadsHandlesMapper
import mega.privacy.android.data.mapper.camerauploads.CameraUploadsHandlesMapperImpl
import mega.privacy.android.data.mapper.camerauploads.SyncRecordTypeIntMapper
import mega.privacy.android.data.mapper.camerauploads.SyncRecordTypeIntMapperImpl
import mega.privacy.android.data.mapper.camerauploads.SyncRecordTypeMapper
import mega.privacy.android.data.mapper.camerauploads.SyncRecordTypeMapperImpl
import mega.privacy.android.data.mapper.camerauploads.UploadOptionIntMapper
import mega.privacy.android.data.mapper.camerauploads.UploadOptionIntMapperImpl
import mega.privacy.android.data.mapper.camerauploads.UploadOptionMapper
import mega.privacy.android.data.mapper.camerauploads.UploadOptionMapperImpl
import mega.privacy.android.data.mapper.changepassword.PasswordStrengthMapper
import mega.privacy.android.data.mapper.changepassword.PasswordStrengthMapperImpl
import mega.privacy.android.data.mapper.contact.ContactDataMapper
import mega.privacy.android.data.mapper.contact.ContactDataMapperImpl
import mega.privacy.android.data.mapper.contact.ContactItemMapper
import mega.privacy.android.data.mapper.contact.ContactItemMapperImpl
import mega.privacy.android.data.mapper.getFileTypeInfo
import mega.privacy.android.data.mapper.getMimeType
import mega.privacy.android.data.mapper.mapBooleanPreference
import mega.privacy.android.data.mapper.mapMegaNodeListToNodeUpdate
import mega.privacy.android.data.mapper.mapMegaUserListToUserUpdate
import mega.privacy.android.data.mapper.mediaplayer.SubtitleFileInfoMapper
import mega.privacy.android.data.mapper.mediaplayer.SubtitleFileInfoMapperImpl
import mega.privacy.android.data.mapper.meeting.ChatCallMapper
import mega.privacy.android.data.mapper.meeting.ChatCallMapperImpl
import mega.privacy.android.data.mapper.shares.AccessPermissionMapper
import mega.privacy.android.data.mapper.shares.AccessPermissionMapperImpl
import mega.privacy.android.data.mapper.storageStateToInt
import mega.privacy.android.data.mapper.syncStatusToInt
import mega.privacy.android.data.mapper.toAccountDetail
import mega.privacy.android.data.mapper.toAccountLevelDetail
import mega.privacy.android.data.mapper.toAccountSession
import mega.privacy.android.data.mapper.toAccountSessionDetail
import mega.privacy.android.data.mapper.toAccountStorageDetail
import mega.privacy.android.data.mapper.toAccountTransferDetail
import mega.privacy.android.data.mapper.toAccountType
import mega.privacy.android.data.mapper.toAchievementsOverview
import mega.privacy.android.data.mapper.toChatFilesFolderUserAttribute
import mega.privacy.android.data.mapper.toChatListItem
import mega.privacy.android.data.mapper.toChatRequest
import mega.privacy.android.data.mapper.toChatRoom
import mega.privacy.android.data.mapper.toContactCredentials
import mega.privacy.android.data.mapper.toContactRequest
import mega.privacy.android.data.mapper.toCountry
import mega.privacy.android.data.mapper.toCountryCallingCodes
import mega.privacy.android.data.mapper.toDuration
import mega.privacy.android.data.mapper.toEvent
import mega.privacy.android.data.mapper.toFolderLoginStatus
import mega.privacy.android.data.mapper.toImage
import mega.privacy.android.data.mapper.toInviteContactRequest
import mega.privacy.android.data.mapper.toLocalPricing
import mega.privacy.android.data.mapper.toMediaStoreFileType
import mega.privacy.android.data.mapper.toMediaStoreFileTypeUri
import mega.privacy.android.data.mapper.toMegaAchievement
import mega.privacy.android.data.mapper.toMegaChatPeerList
import mega.privacy.android.data.mapper.toMegaExceptionModel
import mega.privacy.android.data.mapper.toMegaPurchase
import mega.privacy.android.data.mapper.toMegaSku
import mega.privacy.android.data.mapper.toMyAccountCredentials
import mega.privacy.android.data.mapper.toNode
import mega.privacy.android.data.mapper.toOfflineNodeInformation
import mega.privacy.android.data.mapper.toOnlineStatus
import mega.privacy.android.data.mapper.toPaymentMethodType
import mega.privacy.android.data.mapper.toPaymentPlatformType
import mega.privacy.android.data.mapper.toPricing
import mega.privacy.android.data.mapper.toRecentActionBucket
import mega.privacy.android.data.mapper.toRecentActionBucketList
import mega.privacy.android.data.mapper.toScannedContactLinkResult
import mega.privacy.android.data.mapper.toShareModel
import mega.privacy.android.data.mapper.toStorageState
import mega.privacy.android.data.mapper.toSubscriptionOptionList
import mega.privacy.android.data.mapper.toSubscriptionStatus
import mega.privacy.android.data.mapper.toTransferEventModel
import mega.privacy.android.data.mapper.toTransferModel
import mega.privacy.android.data.mapper.toUserAlert
import mega.privacy.android.data.mapper.toUserCredentialsMapper
import mega.privacy.android.data.mapper.toUserSet
import mega.privacy.android.data.mapper.toUserUserLastGreen
import mega.privacy.android.data.mapper.toVideo
import mega.privacy.android.data.mapper.toVideoAttachment
import mega.privacy.android.data.mapper.toVideoQuality
import mega.privacy.android.data.mapper.verification.SmsPermissionMapper
import mega.privacy.android.data.mapper.verification.SmsPermissionMapperImpl
import mega.privacy.android.data.mapper.videoQualityToInt
import mega.privacy.android.data.mapper.viewtype.ViewTypeMapper
import mega.privacy.android.data.mapper.viewtype.ViewTypeMapperImpl
import mega.privacy.android.domain.entity.Currency
import mega.privacy.android.domain.entity.UserAccount
import mega.privacy.android.domain.entity.preference.StartScreen
import nz.mega.sdk.MegaAccountDetails
import nz.mega.sdk.MegaNode

/**
 * Module for providing mapper dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class MapperModule {

    @Binds
    abstract fun bindSmsPermissionMapper(implementation: SmsPermissionMapperImpl): SmsPermissionMapper

    @Binds
    abstract fun bindContactDataMapper(implementation: ContactDataMapperImpl): ContactDataMapper

    @Binds
    abstract fun bindContactDataItem(implementation: ContactItemMapperImpl): ContactItemMapper

    @Binds
    abstract fun bindUploadOptionMapper(implementation: UploadOptionMapperImpl): UploadOptionMapper

    @Binds
    abstract fun bindUploadOptionIntMapper(implementation: UploadOptionIntMapperImpl): UploadOptionIntMapper

    @Binds
    abstract fun bindViewTypeMapper(implementation: ViewTypeMapperImpl): ViewTypeMapper

    @Binds
    abstract fun bindSyncRecordTypeMapper(implementation: SyncRecordTypeMapperImpl): SyncRecordTypeMapper

    @Binds
    abstract fun bindSyncRecordTypeIntMapper(implementation: SyncRecordTypeIntMapperImpl): SyncRecordTypeIntMapper

    @Binds
    abstract fun bindCameraUploadsHandlesMapper(implementation: CameraUploadsHandlesMapperImpl): CameraUploadsHandlesMapper


    @Binds
    abstract fun bindSortOrderMapper(implementation: SortOrderMapperImpl): SortOrderMapper

    @Binds
    abstract fun bindSortOrderIntMapper(implementation: SortOrderIntMapperImpl): SortOrderIntMapper

    /**
     * Provides AccessPermissionMapper Mapper
     */
    @Binds
    abstract fun bindAccessPermissionMapper(implementation: AccessPermissionMapperImpl): AccessPermissionMapper

    /**
     * Provides PasswordStrength Mapper
     */
    @Binds
    abstract fun bindsPasswordStrengthMapper(implementation: PasswordStrengthMapperImpl): PasswordStrengthMapper

    /**
     * Provides Chat call Mapper
     */
    @Binds
    abstract fun bindChatCallMapper(implementation: ChatCallMapperImpl): ChatCallMapper

    /**
     * Provides Handle list Mapper
     */
    @Binds
    abstract fun bindHandleListMapper(implementation: HandleListMapperImpl): HandleListMapper

    /**
     * Provide Subtitle file info mapper
     */
    @Binds
    abstract fun bindSubtitleFileInfoMapper(implementation: SubtitleFileInfoMapperImpl): SubtitleFileInfoMapper

    @Binds
    abstract fun bindChatScheduledMeetingOccurrMapper(implementation: ChatScheduledMeetingOccurrMapperImpl): ChatScheduledMeetingOccurrMapper

    @Binds
    abstract fun bindChatScheduledMeetingMapper(implementation: ChatScheduledMeetingMapperImpl): ChatScheduledMeetingMapper

    @Binds
    abstract fun bindCombinedChatRoomMapper(implementation: CombinedChatRoomMapperImpl): CombinedChatRoomMapper

    companion object {
        /**
         * Provide account type mapper
         */
        @Provides
        fun provideAccountTypeMapper(): AccountTypeMapper = ::toAccountType

        /**
         * Provide user update mapper
         */
        @Provides
        fun provideUserUpdateMapper(): UserUpdateMapper = ::mapMegaUserListToUserUpdate

        /**
         * Provide user alert mapper
         */
        @Provides
        fun provideUserAlertMapper(): UserAlertMapper = ::toUserAlert

        /**
         * Provide chat request mapper
         */
        @Provides
        fun provideChatRequestMapper(): ChatRequestMapper = ::toChatRequest

        /**
         * Provide start screen mapper
         */
        @Provides
        fun provideStartScreenMapper(): StartScreenMapper = { StartScreen(it) }

        /**
         * Provide user last green mapper
         */
        @Provides
        fun provideUserLastGreenMapper(): UserLastGreenMapper = ::toUserUserLastGreen

        /**
         * Provide mega transfer mapper
         */
        @Provides
        fun provideMegaTransferMapper(): MegaTransferMapper = ::toTransferModel

        /**
         * Provide mega exception mapper
         */
        @Provides
        fun provideMegaExceptionMapper(): MegaExceptionMapper = ::toMegaExceptionModel

        /**
         * Provide transfer event mapper
         */
        @Provides
        fun provideTransferEventMapper(
            exceptionMapper: MegaExceptionMapper,
            transferMapper: MegaTransferMapper,
        ): TransferEventMapper = { event ->
            toTransferEventModel(event, transferMapper, exceptionMapper)
        }

        /**
         * Provide images mapper
         */
        @Provides
        fun provideImagesMapper(): ImageMapper = ::toImage

        /**
         * Provide videos mapper
         */
        @Provides
        fun provideVideosMapper(): VideoMapper = ::toVideo

        /**
         * Provide event mapper
         */
        @Provides
        fun provideEventMapper(): EventMapper = ::toEvent


        /**
         * Provide media store file type mapper
         */
        @Provides
        fun provideMediaStoreFileTypeMapper(): MediaStoreFileTypeMapper = ::toMediaStoreFileType

        /**
         * Provide media store file type uri mapper
         */
        @Provides
        fun provideMediaStoreFileTypeUriMapper(): MediaStoreFileTypeUriMapper =
            ::toMediaStoreFileTypeUri

        /**
         * Provide storage state mapper
         */
        @Provides
        fun provideStorageStateMapper(): StorageStateMapper = ::toStorageState

        /**
         * Provide [StorageState] to [Int] mapper
         */
        @Provides
        fun provideStorageStateIntMapper(): StorageStateIntMapper = ::storageStateToInt

        /**
         * Provide node update mapper
         */
        @Provides
        fun provideNodeUpdateMapper(): NodeUpdateMapper = ::mapMegaNodeListToNodeUpdate

        /**
         * Provide mega chat peer list mapper
         */
        @Provides
        fun provideMegaChatPeerListMapper(): MegaChatPeerListMapper = ::toMegaChatPeerList

        /**
         * Provide online status mapper
         */
        @Provides
        fun provideOnlineStatusMapper(): OnlineStatusMapper = ::toOnlineStatus

        /**
         * Provide mega share mapper
         */
        @Provides
        fun provideMegaShareMapper(): MegaShareMapper = ::toShareModel

        /**
         * Provide boolean preference mapper
         */
        @Provides
        fun provideBooleanPreferenceMapper(): BooleanPreferenceMapper = ::mapBooleanPreference

        /**
         * Provide file type info mapper
         *
         * @param mimeTypeMapper
         */
        @Provides
        fun provideFileTypeInfoMapper(mimeTypeMapper: MimeTypeMapper): FileTypeInfoMapper =
            { node ->
                getFileTypeInfo(node, mimeTypeMapper)
            }

        /**
         * Provide contact request mapper
         */
        @Provides
        fun provideContactRequestMapper(): ContactRequestMapper = ::toContactRequest

        /**
         * Provide mime type mapper
         */
        @Provides
        fun provideMimeTypeMapper(): MimeTypeMapper = { extension ->
            getMimeType(extension, MimeTypeMap.getSingleton()::getMimeTypeFromExtension)
        }


        /**
         * Provide favourite info mapper
         */
        @Provides
        fun provideFavouriteInfoMapper(): NodeMapper = ::toNode


        /**
         * Provide user account mapper
         */
        @Provides
        fun provideUserAccountMapper(): UserAccountMapper = ::UserAccount

        /**
         * Provide local pricing mapper
         */
        @Provides
        fun provideLocalPricingMapper(): LocalPricingMapper = ::toLocalPricing

        /**
         * Provide currency mapper
         */
        @Provides
        fun provideCurrencyMapper(): CurrencyMapper = ::Currency

        /**
         * Provide paymentMethod type mapper
         */
        @Provides
        fun providePaymentMethodTypeMapper(): PaymentMethodTypeMapper = ::toPaymentMethodType

        /**
         * Provide subscription plan list mapper
         */
        @Provides
        fun provideSubscriptionOptionListMapper(): SubscriptionOptionListMapper =
            ::toSubscriptionOptionList

        /**
         * Provide mega achievement mapper
         */
        @Provides
        fun provideMegaAchievementMapper(): MegaAchievementMapper = ::toMegaAchievement

        /**
         * Provide mega achievements detail mapper
         */
        @Provides
        fun provideAchievementsOverviewMapper(): AchievementsOverviewMapper =
            ::toAchievementsOverview

        /**
         * Provide [RecentActionsMapper] mapper
         */
        @Provides
        fun provideRecentActionsMapper(): RecentActionsMapper = ::toRecentActionBucketList

        /**
         * Provide [UserSetMapper] mapper
         */
        @Provides
        fun provideUserSetMapper(): UserSetMapper = ::toUserSet

        /**
         * Provide [RecentActionBucketMapper] mapper
         */
        @Provides
        fun provideRecentActionBucketMapper(): RecentActionBucketMapper = ::toRecentActionBucket

        /**
         * Provide [SubscriptionStatusMapper] mapper
         */
        @Provides
        fun provideSubscriptionStatusMapper(): SubscriptionStatusMapper = ::toSubscriptionStatus

        /**
         * Provide [AccountDetailMapper] mapper
         */
        @Provides
        fun provideAccountDetailMapper(
            accountStorageDetailMapper: AccountStorageDetailMapper,
            accountSessionDetailMapper: AccountSessionDetailMapper,
            accountTransferDetailMapper: AccountTransferDetailMapper,
            accountLevelDetailMapper: AccountLevelDetailMapper,
            accountTypeMapper: AccountTypeMapper,
            subscriptionStatusMapper: SubscriptionStatusMapper,
        ): AccountDetailMapper = {
                details: MegaAccountDetails,
                numDetails: Int,
                rootNode: MegaNode?,
                rubbishNode: MegaNode?,
                inShares: List<MegaNode>,
            ->
            toAccountDetail(
                details,
                numDetails,
                rootNode,
                rubbishNode,
                inShares,
                accountStorageDetailMapper,
                accountSessionDetailMapper,
                accountTransferDetailMapper,
                accountLevelDetailMapper,
                accountTypeMapper,
                subscriptionStatusMapper,
            )
        }

        /**
         * Provide chat room mapper
         */
        @Provides
        fun provideChatRoomMapper(): ChatRoomMapper = ::toChatRoom

        /**
         * Provide [MyAccountCredentialsMapper] mapper
         */
        @Provides
        fun provideMyAccountCredentialsMapper(): MyAccountCredentialsMapper =
            ::toMyAccountCredentials

        /**
         * Provide [ContactCredentialsMapper] mapper
         */
        @Provides
        fun provideContactCredentialsMapper(): ContactCredentialsMapper = ::toContactCredentials

        @Provides
        fun provideOfflineNodeInformationMapper(): OfflineNodeInformationMapper =
            ::toOfflineNodeInformation

        /**
         * Provide [CountryMapper] mapper
         */
        @Provides
        fun provideCountryMapper(): CountryMapper = ::toCountry

        /**
         * Provide chat list item mapper
         */
        @Provides
        fun provideChatListItemMapper(): ChatListItemMapper = ::toChatListItem

        /**
         * Provide country calling codes mapper
         */
        @Provides
        fun provideCountryCallingCodeMapper(): CountryCallingCodeMapper =
            CountryCallingCodeMapper(::toCountryCallingCodes)

        /**
         * Provide video quality mapper
         */
        @Provides
        fun provideVideoQualityMapper(): VideoQualityMapper = ::toVideoQuality

        /**
         * Provide pricing mapper
         *
         */
        @Provides
        fun providePricingMapper(): PricingMapper = ::toPricing

        /**
         * Provide video quality int mapper
         */
        @Provides
        fun provideVideoQualityIntMapper(): VideoQualityIntMapper = ::videoQualityToInt

        @Provides
        fun provideSyncStatusIntMapper(): SyncStatusIntMapper = ::syncStatusToInt

        @Provides
        fun provideMegaSkuMapper(): MegaSkuMapper = ::toMegaSku

        @Provides
        fun provideMegaPurchaseMapper(): MegaPurchaseMapper = ::toMegaPurchase

        @Provides
        fun provideChatFilesFolderUserAttributeMapper(): ChatFilesFolderUserAttributeMapper =
            ::toChatFilesFolderUserAttribute

        /**
         * Provide subscription platform type mapper
         */
        @Provides
        fun provideSubscriptionPlatformTypeMapper(): PaymentPlatformTypeMapper =
            ::toPaymentPlatformType

        /**
         * Provide account transfer detail mapper
         */
        @Provides
        fun provideAccountTransferDetailMapper(): AccountTransferDetailMapper =
            ::toAccountTransferDetail

        /**
         * Provide account session detail mapper
         */
        @Provides
        fun provideAccountSessionDetailMapper(): AccountSessionDetailMapper =
            ::toAccountSessionDetail

        /**
         * Provide account storage detail mapper
         */
        @Provides
        fun provideAccountStorageDetailMapper(): AccountStorageDetailMapper =
            ::toAccountStorageDetail

        /**
         * Provide account level detail mapper
         */
        @Provides
        fun provideAccountLevelDetailMapper(): AccountLevelDetailMapper = ::toAccountLevelDetail

        /**
         * Provide file duration mapper
         */
        @Provides
        fun provideFileDurationMapper(): FileDurationMapper = ::toDuration

        /**
         * Provides scanned contact link result mapper
         */
        @Provides
        fun provideScannedContactLinkResultMapper(): ScannedContactLinkResultMapper =
            ::toScannedContactLinkResult

        /**
         * Provides invite contact request mapper
         */
        @Provides
        fun provideInviteContactRequestMapper(): InviteContactRequestMapper =
            ::toInviteContactRequest

        /**
         * Provides user credentials mapper
         */
        @Provides
        fun provideUserCredentialsMapper(): UserCredentialsMapper = ::toUserCredentialsMapper

        /**
         * Provides account session mapper
         */
        @Provides
        fun provideAccountSessionMapper(): AccountSessionMapper = ::toAccountSession

        /**
         * Provides VideoAttachment mapper
         */
        @Provides
        fun provideVideoAttachmentMapper(): VideoAttachmentMapper = ::toVideoAttachment

        /**
         * Provides FolderLoginStatus mapper
         */
        @Provides
        fun provideFolderLoginStatusMapper(): FolderLoginStatusMapper = ::toFolderLoginStatus
    }
}
