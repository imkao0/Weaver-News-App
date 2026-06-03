package dev.mkao.weaver.presentation.common.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.mkao.weaver.designsystem.R
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.presentation.common.components.feedback.sourceChipColor
import dev.mkao.weaver.util.AppConstants
import dev.mkao.weaver.util.Dimens
import dev.mkao.weaver.util.articleDateFormat

@Composable
fun CardArticle(
    article: Article,
    onReadFullStoryClicked: () -> Unit,
) {
    val date = articleDateFormat(article.publishedAt)
    val chip = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .padding(Dimens.PaddingMicro / 2)
            .fillMaxWidth()
            .height(Dimens.ArticleCardHeight)
            .clickable { onReadFullStoryClicked() },
        colors = CardDefaults.cardColors(
            containerColor = chip.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(Dimens.PaddingMicro)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                modifier = Modifier
                    .height(Dimens.ArticleCardHeight)
                    .width(Dimens.ArticleCardImageWidth),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(article.image)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
            Column(
                verticalArrangement = Arrangement.SpaceAround,
                modifier = Modifier
                    .padding(horizontal = Dimens.ExtraSmallPadding)
                    .fillMaxWidth(),

            ) {
                val sourceName = article.source.name ?: "Latest News"
                Box(
                    modifier = Modifier
                        .padding(Dimens.PaddingSmall)
                        .height(Dimens.PaddingLarge + Dimens.PaddingSmall + 2.dp)
                        .background(sourceChipColor(sourceName), shape = RoundedCornerShape(Dimens.RadiusSmall)),
                ) {
                    Spacer(modifier = Modifier.height(Dimens.PaddingMicro))
                    Text(
                        text = sourceName,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = Dimens.PaddingSmall),
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.PaddingTiny))
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = chip.onSurface,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(Dimens.PaddingTiny))
                Text(
                    text = article.description ?: stringResource(R.string.Error404),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = AppConstants.MaxLinesTitle,
                    color = chip.onSurfaceVariant,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(Dimens.ExtraExtraPadding))
                    Text(
                        text = ".",
                        color = chip.onSurface,
                        fontWeight = FontWeight.Bold,
                    )

                    Spacer(modifier = Modifier.width(Dimens.ExtraExtraPadding))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_time),
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.PaddingLarge - 1.dp),
                        tint = chip.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(Dimens.ExtraSmallPadding))
                    Text(
                        modifier = Modifier.align(alignment = Alignment.CenterVertically),
                        text = date,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = chip.onSurface,
                    )
                }
            }
        }
    }
}
