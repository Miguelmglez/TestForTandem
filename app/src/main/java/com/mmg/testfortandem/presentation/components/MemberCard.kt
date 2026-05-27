package com.mmg.testfortandem.presentation.components
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mmg.testfortandem.R
import com.mmg.testfortandem.domain.model.Language
import com.mmg.testfortandem.domain.model.LikedMember
import com.mmg.testfortandem.presentation.theme.BadgeNew
import com.mmg.testfortandem.presentation.theme.LikeActive
import com.mmg.testfortandem.presentation.theme.TextPrimary
import com.mmg.testfortandem.presentation.theme.TextSecondary
import com.mmg.testfortandem.presentation.theme.TextTertiary
import java.util.Locale

@Composable
fun MemberCard(
    likedMember: LikedMember,
    onLikeClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val member = likedMember.member
    val learnsPhrase = languagesPhrase(member.learns)
    val nativeCode = member.natives.firstOrNull()?.isoCode?.uppercase().orEmpty()
    val firstLearnCode = member.learns.firstOrNull()?.isoCode?.uppercase().orEmpty()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        AsyncImage(
            model = member.pictureUrl,
            contentDescription = stringResource(
                R.string.member_picture_content_description,
                member.firstName,
            ),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(4.dp)),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            // Header row: name + (NEW badge or referenceCnt)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = member.firstName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                if (member.isNew) {
                    NewBadge()
                } else {
                    Text(
                        text = member.referenceCnt.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.member_bio_format, learnsPhrase),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom row: NATIVE / LEARNS / like
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LanguageBadge(
                    label = stringResource(R.string.member_native_label),
                    value = nativeCode,
                )
                LanguageBadge(
                    label = stringResource(R.string.member_learns_label),
                    value = firstLearnCode,
                )
                Box(modifier = Modifier.weight(1f))
                LikeButton(
                    isLiked = likedMember.isLiked,
                    memberName = member.firstName,
                    onClick = { onLikeClick(member.id) },
                )
            }
        }
    }


}
@Composable
private    fun LikeButton(
    isLiked: Boolean,
    memberName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(
        if (isLiked) R.string.member_unlike_content_description
        else R.string.member_like_content_description,
        memberName,
    )
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = if (isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
            contentDescription = description,
            tint = if (isLiked) LikeActive else TextSecondary,
        )
    }
}

@Composable
private fun NewBadge(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.member_badge_new),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BadgeNew)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun LanguageBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
        )
    }
}

@Composable
private fun languagesPhrase(languages: List<Language>): String {
    val and = stringResource(R.string.language_separator_and)
    val comma = stringResource(R.string.language_separator_comma)
    val names = languages.map { it.displayName(Locale.getDefault()) }
    return joinHumanReadable(names, separator = comma, lastSeparator = and)
}

internal fun Language.displayName(locale: Locale): String =
    Locale.forLanguageTag(isoCode).getDisplayLanguage(locale).ifBlank { isoCode.uppercase() }

internal fun joinHumanReadable(
    items: List<String>,
    separator: String,
    lastSeparator: String,
): String = when (items.size) {
    0 -> ""
    1 -> items.first()
    2 -> items.joinToString(lastSeparator)
    else -> items.dropLast(1).joinToString(separator) + lastSeparator + items.last()
}
